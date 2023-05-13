package de.hub.mse.server.repository;

import de.hub.mse.server.management.FileDescriptor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileDescriptorRepository extends JpaRepository<FileDescriptor, String> {
}
