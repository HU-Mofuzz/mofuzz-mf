package de.hub.mse.client.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionResult {

    private String id;

    private String experiment;
    private String originClient;
    private String fileDescriptor;

    private String previousFile;

    private String exception;

    private boolean crash;

    private boolean hang;

    private int errorCount;

    private long duration;

    private long timestamp;
}
