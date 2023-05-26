package de.hub.mse.server.service.execution;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.PoolBasedGenerator;
import de.hub.mse.emf.multifile.impl.opendocument.LinkedFile;
import de.hub.mse.server.management.Experiment;
import de.hub.mse.server.management.FileDescriptor;
import de.hub.mse.server.repository.FileDescriptorRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class MofuzzFileGenerator<G extends PoolBasedGenerator<? extends LinkedFile, String, ?, ?>> implements FileGenerator {

    private final G generator;
    private final FileDescriptorRepository fileRepository;
    private final FilePersistence persistence;

    private final SourceOfRandomness randomness = new SourceOfRandomness(new Random());

    public MofuzzFileGenerator(G generator, FileDescriptorRepository fileRepository,
                               FilePersistence persistence) {
        this.generator = generator;
        this.fileRepository = fileRepository;
        this.persistence = persistence;
    }

    private static String fileToId(File file) {
        // Get position of last '.'.
        int pos = file.getName().lastIndexOf(".");

        if (pos == -1) {
            // If there wasn't any '.' just return the string as is.
            return file.getName();
        } else {
            // Otherwise return the string, up to the dot.
            return file.getName().substring(0, pos);
        }
    }

    private static FileDescriptor descriptorForLinkedFile(LinkedFile xlsxFile, Experiment experiment) {
        return FileDescriptor.builder()
                .id(fileToId(xlsxFile.getMainFile()))
                .linkedFiles(xlsxFile.getLinkedFiles().stream()
                        .map(MofuzzFileGenerator::fileToId)
                        .toList())
                .depth(xlsxFile.getDepth())
                .experiment(experiment.getId())
                .documentHeight(experiment.getDocumentHeight())
                .documentWidth(experiment.getDocumentWidth())
                .sheetCount(experiment.getSheetsPerDocument())
                .build();
    }

    @Override
    public void prepareExecution(Experiment experiment) throws Exception {
        var files = generator.prepareLinkPool(randomness);
        for(var xlsxFile : files) {
            var fileDescriptor = descriptorForLinkedFile(xlsxFile, experiment);
            fileRepository.save(fileDescriptor);
            persistence.persistFile(fileDescriptor.getId(), xlsxFile.getMainFile());
            xlsxFile.getMainFile().delete();
        }

        List<String> serializedLinks = new ArrayList<>();
        for(var link : generator.getConfig().getLinkPool()) {
            serializedLinks.add(link.serializeLink());
        }
        experiment.setSerializedLinks(serializedLinks);
    }

    @Override
    public FileDescriptor generateFileBlocking(Experiment experiment) {
        try {
            var file = this.generator.generate(randomness, null);
            var fileDescriptor = FileDescriptor.builder()
                            .build();
            fileDescriptor = fileRepository.save(fileDescriptor);
            persistence.persistFile(fileDescriptor.getId(), file.getMainFile());
            file.getMainFile().delete();
            return fileDescriptor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void generateBatch(int batchSize, Experiment experiment) {
        for(int i = 0; i < batchSize; i++) {
            var xlsxFile = generator.generate(randomness, null);
            var fileDescriptor = descriptorForLinkedFile(xlsxFile, experiment);
            try {
                persistence.persistFile(fileDescriptor.getId(), xlsxFile.getMainFile());
                fileRepository.save(fileDescriptor);
                xlsxFile.getMainFile().delete();
            } catch (Exception e) {
                // on exception remove from db and move on
                log.error("Error generating file in batch: ", e);
                persistence.deleteFile(fileDescriptor.getId());
                fileRepository.delete(fileDescriptor);
            }
        }
    }

    @Override
    public void reIndexPoolOfExperiment(Experiment experiment) {
        generator.getConfig().getLinkPool().clear();
         for(String link : experiment.getSerializedLinks()) {
            generator.addLinkFromSerialized(link);
        }
    }
}
