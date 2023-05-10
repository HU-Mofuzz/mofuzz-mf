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
@Table(name = "execution_results", indexes = {
        @Index(columnList = "experiment"),
        @Index(columnList = "originClient"),
        @Index(columnList = "experiment, originClient")
})
public class ExecutionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String experiment;
    private String originClient;
    private String fileDescriptor;

    private  boolean crash;

    private boolean hang;

    private int errorCount;

    private long duration;

    private long timestamp;
}
