package de.hub.mse.server.repository;

import de.hub.mse.server.management.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, String> {

}
