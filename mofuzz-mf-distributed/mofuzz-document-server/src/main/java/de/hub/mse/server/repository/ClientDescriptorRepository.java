package de.hub.mse.server.repository;

import de.hub.mse.server.management.ClientDescriptor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientDescriptorRepository extends JpaRepository<ClientDescriptor, String> {
}
