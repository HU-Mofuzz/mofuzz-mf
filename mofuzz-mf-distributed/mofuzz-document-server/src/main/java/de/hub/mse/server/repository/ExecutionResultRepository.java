package de.hub.mse.server.repository;

import de.hub.mse.server.management.ExecutionResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionResultRepository extends JpaRepository<ExecutionResult, String> {
}
