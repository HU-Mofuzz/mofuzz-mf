package de.hub.mse.server.repository;

import de.hub.mse.server.management.ExecutionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionResultRepository extends JpaRepository<ExecutionResult, String> {

    int countByExperimentIsAndOriginClient(String experiment, String originClient);

    @Query(value = "select file_descriptor from execution_results where experiment = :experiment and origin_client = :origin", nativeQuery = true)
    List<String> getFileIdsByExperimentAndClient(@Param("experiment") String experimentId,
                                                        @Param("origin") String clientId);

    boolean existsByExperimentAndOriginClientAndFileDescriptor(String experiment, String originClient, String fileDescriptor);

    List<ExecutionResult> findAllByExperiment(String experiment);
}
