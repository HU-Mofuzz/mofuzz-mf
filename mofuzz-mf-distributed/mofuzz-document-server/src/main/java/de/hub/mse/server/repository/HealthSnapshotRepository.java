package de.hub.mse.server.repository;

import de.hub.mse.server.management.HealthSnapshot;
import org.springframework.data.repository.CrudRepository;

public interface HealthSnapshotRepository extends CrudRepository<HealthSnapshot, String> {
}
