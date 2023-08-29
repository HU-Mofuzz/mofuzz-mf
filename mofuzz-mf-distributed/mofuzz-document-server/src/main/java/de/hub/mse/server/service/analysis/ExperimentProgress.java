package de.hub.mse.server.service.analysis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExperimentProgress {
    private int existingResults;
    private int generatedDocuments;
    private int totalDocumentCount;
}
