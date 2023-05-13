package de.hub.mse.server.repository;

import de.hub.mse.server.management.HealthSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthSnapshotRepository extends JpaRepository<HealthSnapshot, String> {
}
