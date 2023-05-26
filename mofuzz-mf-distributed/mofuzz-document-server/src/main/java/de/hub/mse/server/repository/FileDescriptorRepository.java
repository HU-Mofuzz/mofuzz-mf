package de.hub.mse.server.repository;

import de.hub.mse.server.management.FileDescriptor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileDescriptorRepository extends JpaRepository<FileDescriptor, String> {

    int countByExperimentIsAndDepth(String experimentId, int depth);

    @Query(value = "select id from file_descriptor where experiment = :experiment", nativeQuery = true)
    List<String> getFileIdsForExperiment(@Param("experiment") String experimentId);

    List<FileDescriptor> getByExperimentAndDepthLessThan(String experiment, int depth);
}
