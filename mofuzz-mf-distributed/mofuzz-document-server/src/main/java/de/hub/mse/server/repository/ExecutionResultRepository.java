package de.hub.mse.server.repository;

import de.hub.mse.server.management.ExecutionResult;
import de.hub.mse.server.service.analysis.ClientResultCount;
import de.hub.mse.server.service.analysis.ExceptionCount;
import de.hub.mse.server.service.analysis.ResultDuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionResultRepository extends JpaRepository<ExecutionResult, String> {

    @Query(value = "select file_descriptor from execution_results where experiment = :experiment and origin_client = :origin", nativeQuery = true)
    List<String> getFileIdsByExperimentAndClient(@Param("experiment") String experimentId,
                                                        @Param("origin") String clientId);

    boolean existsByExperimentAndOriginClientAndFileDescriptor(String experiment, String originClient, String fileDescriptor);

    List<ExecutionResult> findAllByExperiment(String experiment);

    Page<ExecutionResult> findAllByExperiment(String experiment, Pageable pageable);

    Page<ExecutionResult> findAllByExperimentAndOriginClient(String experiment, String originClient, Pageable pageable);


    // Count in general
    int countByExperimentIsAndOriginClient(String experiment, String originClient);
    int countByExperiment(String experiment);

    // count crashes
    int countByCrashTrueAndHangFalseAndExperiment(String experiment);
    int countByCrashTrueAndHangFalseAndExperimentAndOriginClient(String experiment, String originClient);

    // Count hangs
    int countByHangTrueAndExperiment(String experiment);
    int countByHangTrueAndExperimentAndOriginClient(String experiment, String originClient);

    // Count NO HANG and NO CRASH
    int countByHangFalseAndCrashFalseAndExperiment(String experiment);
    int countByHangFalseAndCrashFalseAndExperimentAndOriginClient(String experiment, String originClient);

    // get unique crashes with count
    @Query("select new de.hub.mse.server.service.analysis.ExceptionCount(e.exception, COUNT(e.exception)) " +
            "from ExecutionResult as e where e.exception <> '' and e.experiment = :experiment group by e.exception")
    List<ExceptionCount> getUniqueStacktracesWithCount(@Param("experiment") String experimentId);

    @Query("select new de.hub.mse.server.service.analysis.ExceptionCount(e.exception, COUNT(e.exception)) " +
            "from ExecutionResult as e where e.exception <> '' and e.experiment = :experiment and e.originClient = :client group by e.exception")
    List<ExceptionCount> getUniqueStacktracesWithCountForClient(@Param("experiment") String experimentId,
                                                                @Param("client") String clientId);

    // execution durations
    @Query("select new de.hub.mse.server.service.analysis.ResultDuration(e.fileDescriptor, e.duration) from ExecutionResult as e " +
            "where e.experiment = :experiment order by e.duration desc limit 1")
    ResultDuration getLongestDuration(@Param("experiment") String experimentId);

    @Query("select new de.hub.mse.server.service.analysis.ResultDuration(e.fileDescriptor, e.duration) from ExecutionResult as e " +
            "where e.experiment = :experiment and e.originClient = :client order by e.duration desc limit 1")
    ResultDuration getLongestDurationForClient(@Param("experiment") String experimentId,
                                      @Param("client") String clientId);

    @Query("select new de.hub.mse.server.service.analysis.ResultDuration(e.fileDescriptor, e.duration) from ExecutionResult as e " +
            "where e.experiment = :experiment order by e.duration asc limit 1")
    ResultDuration getShortestDuration(@Param("experiment") String experimentId);

    @Query("select new de.hub.mse.server.service.analysis.ResultDuration(e.fileDescriptor, e.duration) from ExecutionResult as e " +
            "where e.experiment = :experiment and e.originClient = :client order by e.duration asc limit 1")
    ResultDuration getShortestDurationForClient(@Param("experiment") String experimentId,
                                      @Param("client") String clientId);

    @Query("select AVG(e.duration) from ExecutionResult as e " +
            "where e.experiment = :experiment")
    double getAverageDuration(@Param("experiment") String experimentId);

    @Query("select AVG(e.duration) from ExecutionResult as e " +
            "where e.experiment = :experiment and e.originClient = :client")
    double getAverageDurationForClient(@Param("experiment") String experimentId,
                                       @Param("client") String clientId);

    @Query("select new de.hub.mse.server.service.analysis.ClientResultCount(e.originClient, COUNT(e.originClient)) " +
            "from ExecutionResult as e where e.experiment = :experiment group by e.originClient")
    List<ClientResultCount> getClientsWithResultsForExperiment(@Param("experiment") String experimentId);
}
