package de.hub.mse.server.repository;

import de.hub.mse.server.management.ClientDescriptor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientDescriptorRepository extends JpaRepository<ClientDescriptor, String> {
}
