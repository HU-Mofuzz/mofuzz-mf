package de.hub.mse.server.repository;

import de.hub.mse.server.management.Experiment;
import org.springframework.data.repository.CrudRepository;

public interface ExperimentRepository extends CrudRepository<Experiment, String> {
}
