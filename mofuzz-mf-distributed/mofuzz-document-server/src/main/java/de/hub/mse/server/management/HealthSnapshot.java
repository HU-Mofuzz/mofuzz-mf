package de.hub.mse.server.management;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "health_snapshot", indexes = {
        @Index(columnList = "system"),
        @Index(columnList = "timestamp"),
        @Index(columnList = "system, timestamp")
})
public class HealthSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String system;

    private long timestamp;
    private Double cpu;
    private Double memory;
    private Double disk;
}
