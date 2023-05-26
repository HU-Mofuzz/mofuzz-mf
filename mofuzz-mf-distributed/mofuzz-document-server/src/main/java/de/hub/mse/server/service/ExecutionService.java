package de.hub.mse.server.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.hub.mse.emf.multifile.impl.opendocument.XlsxGenerator;
import de.hub.mse.emf.multifile.impl.opendocument.XlsxGeneratorConfig;
import de.hub.mse.server.config.ServiceConfig;
import de.hub.mse.server.exceptions.NotFoundException;
import de.hub.mse.server.management.ClientDescriptor;
import de.hub.mse.server.management.Experiment;
import de.hub.mse.server.management.FileDescriptor;
import de.hub.mse.server.repository.ClientDescriptorRepository;
import de.hub.mse.server.repository.ExecutionResultRepository;
import de.hub.mse.server.repository.ExperimentRepository;
import de.hub.mse.server.repository.FileDescriptorRepository;
import de.hub.mse.server.service.execution.AwsPersistence;
import de.hub.mse.server.service.execution.FileGenerator;
import de.hub.mse.server.service.execution.FilePersistence;
import de.hub.mse.server.service.execution.MofuzzFileGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ExecutionService {

    private static final int FILE_DELTA_START_GENERATING = 50;
    private static final int FILE_BATCH_TO_GENERATE = 100;

    private static final int EXECUTOR_CORE_POOL_SIZE = 4;

    private static final int WARNING_MIN_INTERVAL_MINUTES = 5;

    private final ClientDescriptorRepository clientRepository;
    private final FileDescriptorRepository fileRepository;

    private final ExperimentRepository experimentRepository;
    private final ExecutionResultRepository resultRepository;
    private final MailService mailService;
    private final ServiceConfig serviceConfig;

    private final ExecutorService executor = Executors.newFixedThreadPool(EXECUTOR_CORE_POOL_SIZE);
    private final FilePersistence filePersistence = new AwsPersistence();

    private final Set<String> generatingExperiments = Sets.newConcurrentHashSet();
    private final Set<String> indexingExperiments = Sets.newConcurrentHashSet();
    private final Set<String> indexedExperiments = Sets.newConcurrentHashSet();

    private final Map<String, Long> lastWarningTimestamps = Maps.newConcurrentMap();

    private final Map<String, FileGenerator> experimentGenerators = Maps.newConcurrentMap();


    @Autowired
    public ExecutionService(ClientDescriptorRepository clientRepository, FileDescriptorRepository fileRepository,
                            ExperimentRepository experimentRepository, ExecutionResultRepository resultRepository,
                            MailService mailService, ServiceConfig serviceConfig) {
        this.clientRepository = clientRepository;
        this.fileRepository = fileRepository;
        this.experimentRepository = experimentRepository;
        this.resultRepository = resultRepository;
        this.mailService = mailService;
        this.serviceConfig = serviceConfig;
    }


    private FileGenerator getFileGeneratorForExperiment(Experiment experiment) {
        FileGenerator generator;
        if(experimentGenerators.containsKey(experiment.getId())) {
            generator = experimentGenerators.get(experiment.getId());
        } else {
            generator = new MofuzzFileGenerator<>(new XlsxGenerator(XlsxGeneratorConfig.builder()
                    .workingDirectory(serviceConfig.getDocumentDirectory())
                    .modelWidth(experiment.getDocumentWidth())
                    .modelHeight(experiment.getDocumentHeight())
                    .targetDocumentDepth(experiment.getTreeDepth())
                    .sheetsPerDocument(experiment.getSheetsPerDocument())
                    .build()), fileRepository, filePersistence);
            experimentGenerators.put(experiment.getId(), generator);
        }
        return generator;
    }


    public Optional<FileDescriptor> getNextFileDescriptorForClient(String clientId) throws NotFoundException {

        // determine client
        var client = clientRepository.findById(clientId).orElseThrow(NotFoundException::new);
        if(!client.hasAssignedExperiments()) {
            return Optional.empty();
        }

        // determine experiment
        var experimentId = client.getCurrentExperiment();
        Experiment experiment;
        var foundExperiment = experimentRepository.findById(client.getCurrentExperiment());
        if(foundExperiment.isPresent()) {
            experiment = foundExperiment.get();
        } else {
            // Experiment somehow got deleted, move on to the next one
            moveClientToNextExperiment(client);
            return getNextFileDescriptorForClient(clientId);
        }

        // determine the document to return
        int existingExperimentFileCount = fileRepository.countByExperimentIsAndDepth(experimentId, experiment.getTreeDepth());
        if(existingExperimentFileCount == 0 || experiment.getPrepared() == Experiment.PreparationState.PREPARING) {
            if(experiment.getPrepared() == Experiment.PreparationState.UNPREPARED) {
                experiment.setPrepared(Experiment.PreparationState.PREPARING);
                experimentRepository.save(experiment);
                prepareExperiment(experiment, clientId);
            }
            return Optional.empty();
        }
        if(!indexedExperiments.contains(experimentId)) {
            if(!indexingExperiments.contains(experimentId)) {
                indexingExperiments.add(experimentId);
                startExperimentIndexing(experiment);
            }
            return Optional.empty();
        }
        if(existingExperimentFileCount < experiment.getDocumentCount()) {
            // There are still files to generate left
            var candidateFileDescriptor = findFileDescriptorForClientWithNoResult(client, experiment);
            if(candidateFileDescriptor.isPresent()) {
                // An already generated candidate was found, this should be the normal case
                startFileGenerationIfNecessary(experiment, clientId, existingExperimentFileCount);
                return candidateFileDescriptor;
            } else {
                // there are still files to generate left, but none of them is prepared for this client
                // this state should be avoided at all costs! Since now all clients have to actively wait for the generator
                log.error("Run out of files to generate for experiment [{}], falling back to blocking generation!",
                        experiment.getDescription());
                var fileGenerator = getFileGeneratorForExperiment(experiment);
                synchronized (fileGenerator) {
                    // now everyone has to wait for a single file to generated first
                    // count again in case you were the waiting party
                    var newExistingExperimentFileCount = fileRepository.countByExperimentIsAndDepth(experimentId, experiment.getTreeDepth());
                    if(newExistingExperimentFileCount == existingExperimentFileCount) {
                        // this is the branch for the first critical party
                        sendWarningMailAsync("[Mofuzz] Important experiment warning", String.format("""
                            The experiment [%s] got into a critical state where no files were prepared to be picket up.
                            This should this continue there need to be setup changes due to ongoing synchronous waiting periods.
                            """, experiment.getDescription()), experiment);
                        return Optional.of(forceCriticalStateBlockingGenerationForExperiment(experiment));
                    } else {
                        // this is the branch for every following critical state party
                        return findFileDescriptorForClientWithNoResult(client, experiment);
                    }
                }
            }
        } else {
            // no files to generate left
            int currentResultCount = resultRepository.countByExperimentIsAndOriginClient(experimentId, clientId);
            if(currentResultCount < experiment.getDocumentCount()) {
                // find file descriptor no result exists for
                return findFileDescriptorForClientWithNoResult(client, experiment);
            } else {
                // all results were received move to next experiment
                moveClientToNextExperiment(client);
                return getNextFileDescriptorForClient(clientId);
            }
        }
    }


    private void moveClientToNextExperiment(ClientDescriptor client) {
        var newExperimentId = client.moveToNextExperiment();
        clientRepository.save(client);
        if(newExperimentId == null) {
            log.info("Client [{}] exhausted all assigned experiments!", client.getName());
            sendNotificationMailAsync("[Mofuzz] Experiment update", String.format("""
                    Client %s exhausted all assigned experiments!
                    """, client.getName()));
        } else {
            experimentRepository.findById(newExperimentId).ifPresent(experiment -> {
                log.info("Client [{}] moved to next experiment [{}]", client.getName(),
                        experiment.getDescription());
                sendNotificationMailAsync("[Mofuzz] Experiment update", String.format("""
                    Client [%s] moved to next experiment [%s]
                    """, client.getName(), experiment.getDescription()));
            });
        }
    }

    private Optional<FileDescriptor> findFileDescriptorForClientWithNoResult(ClientDescriptor client,
                                                                             Experiment experiment) {
        var existingResultFileIds = resultRepository.getFileIdsByExperimentAndClient(experiment.getId(), client.getId());
        var experimentFileIds = fileRepository.getFileIdsForExperiment(experiment.getId());
        var uniqueIds = new HashSet<>(experimentFileIds);
        existingResultFileIds.forEach(uniqueIds::remove);
        if(uniqueIds.isEmpty()) {
            return Optional.empty();
        } else {
            return fileRepository.findById(uniqueIds.iterator().next());
        }
    }

    private FileDescriptor forceCriticalStateBlockingGenerationForExperiment(Experiment experiment) {
        try {
            var descriptor = getFileGeneratorForExperiment(experiment).generateFileBlocking(experiment);
            descriptor.setId(null);
            fileRepository.save(descriptor);
            return descriptor;
        } catch (Exception e) {
            log.error("Error in critical state of generation, you're basically doomed...");
            throw new IllegalStateException(e);
        }
    }

    private void startFileGenerationIfNecessary(Experiment experiment, String clientId, int existingExperimentFileCount) {
        executor.submit(() -> {
            int currentResultCount = resultRepository.countByExperimentIsAndOriginClient(experiment.getId(), clientId);
            var delta = existingExperimentFileCount - currentResultCount;
            if(delta < FILE_DELTA_START_GENERATING && !generatingExperiments.contains(experiment.getId())) {
                try {
                    log.info("Generating new batch of {} files for experiment [{}]", FILE_BATCH_TO_GENERATE, experiment.getDescription());
                    generatingExperiments.add(experiment.getId());
                    int batchSize = Math.min(FILE_BATCH_TO_GENERATE, experiment.getDocumentCount() - existingExperimentFileCount);
                    getFileGeneratorForExperiment(experiment).generateBatch(batchSize, experiment);
                    log.info("Finished generating new batch of {} files for experiment [{}]", FILE_BATCH_TO_GENERATE, experiment.getDescription());
                } catch (Exception e) {
                    log.error("Error generating next batch of files for experiment [{}]", experiment.getDescription());
                } finally {
                    generatingExperiments.remove(experiment.getId());
                }
            }
        });
    }

    private void prepareExperiment(Experiment experiment, String clientId) {
        executor.submit(() -> {
            try {
                log.info("Preparing experiment [{}]", experiment.getDescription());
                getFileGeneratorForExperiment(experiment).prepareExecution(experiment);
                startFileGenerationIfNecessary(experiment, clientId, 0);
                experiment.setPrepared(Experiment.PreparationState.PREPARED);
                experimentRepository.save(experiment);
                indexedExperiments.add(experiment.getId());
                log.info("Finished preparing experiment [{}]", experiment.getDescription());
            } catch (Exception e) {
                log.error("Error preparing experiment [{}], retrying next time!", experiment.getId(), e);
                for(String id : fileRepository.getFileIdsForExperiment(experiment.getId())) {
                    fileRepository.deleteById(id);
                    filePersistence.deleteFile(id);
                }
                experiment.setPrepared(Experiment.PreparationState.UNPREPARED);
                experimentRepository.save(experiment);
            }
        });
    }

    private void startExperimentIndexing(Experiment experiment) {
        executor.submit(() -> {
            try {
                log.info("Indexing experiment [{}]", experiment.getDescription());
                getFileGeneratorForExperiment(experiment).reIndexPoolOfExperiment(experiment);
                indexedExperiments.add(experiment.getId());
                log.info("Finished indexing experiment [{}]", experiment.getDescription());
            } catch (Exception e) {
                log.error("Error indexing eperiment!", e);
            } finally {
                indexingExperiments.remove(experiment.getId());
            }
        });
    }

    private void sendWarningMailAsync(String title, String message, Experiment experiment) {
        var lastWarning = lastWarningTimestamps.getOrDefault(experiment.getId(), 0L);
        if(lastWarning < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(WARNING_MIN_INTERVAL_MINUTES)) {
            sendNotificationMailAsync(title, message);
            lastWarningTimestamps.put(experiment.getId(), System.currentTimeMillis());
        }
    }

    private void sendNotificationMailAsync(String title, String message) {
        executor.submit(() -> {
            mailService.sendSimpleMessage(title, message);
        });
    }

    public Set<String> getRecursiveFileIdsOfDescriptor(FileDescriptor descriptor) {
        Set<String> resultingIds = new HashSet<>();
        Set<String> inspectedIds = new HashSet<>();
        collectFileTreeRecursive(descriptor, resultingIds, inspectedIds);
        return resultingIds;
    }

    private void collectFileTreeRecursive(FileDescriptor descriptor, Set<String> resultingIds, Set<String> inspectedIds) {
        if(descriptor == null) {
            return;
        }
        inspectedIds.add(descriptor.getId());
        for(String id : descriptor.getLinkedFiles()) {
            if(inspectedIds.contains(id)) {
                continue;
            }
            resultingIds.add(id);
            collectFileTreeRecursive(fileRepository.findById(id).orElse(null), resultingIds, inspectedIds);
        }
    }

}
