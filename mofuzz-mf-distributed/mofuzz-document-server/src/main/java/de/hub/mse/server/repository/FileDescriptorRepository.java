package de.hub.mse.server.repository;

import de.hub.mse.server.management.FileDescriptor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileDescriptorRepository extends JpaRepository<FileDescriptor, String> {

    int countByExperimentIsAndDepth(String experiment, int depth);

    @Query(value = "select id from FileDescriptor experiment = :experiment")
    List<String> getFileIdsForExperiment(@Param("experiment") String experimentId);

    @Query(value = "select id from FileDescriptor where experiment = :experiment and depth = :depth")
    List<String> getFileIdsForExperimentAndDepth(@Param("experiment") String experimentId, @Param("depth") int depth);

    List<FileDescriptor> getByExperimentAndDepthLessThan(String experiment, int depth);
}
