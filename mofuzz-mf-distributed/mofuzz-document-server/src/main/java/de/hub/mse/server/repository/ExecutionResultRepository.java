package de.hub.mse.server.repository;

import de.hub.mse.server.management.ExecutionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionResultRepository extends JpaRepository<ExecutionResult, String> {
}
