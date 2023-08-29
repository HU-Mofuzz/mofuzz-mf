package de.hub.mse.server.repository;

import de.hub.mse.server.management.HealthSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Repository
public interface HealthSnapshotRepository extends JpaRepository<HealthSnapshot, String> {

    @Query("select snapshot from HealthSnapshot as snapshot " +
            "where snapshot.system = :system and snapshot.timestamp >= :start and snapshot.timestamp <= :end " +
            "order by snapshot.timestamp asc")
    Stream<HealthSnapshot> getSnapshotsInTimespan(@Param("system") String system,
                                                  @Param("start") long start,
                                                  @Param("end") long end);
}
