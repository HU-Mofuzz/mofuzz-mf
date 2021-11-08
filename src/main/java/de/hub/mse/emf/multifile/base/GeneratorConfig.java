package de.hub.mse.emf.multifile.base;

import lombok.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@AllArgsConstructor
@Data
@Builder
public class GeneratorConfig {

    private PreparationMode preparationMode;

    private String workingDirectory;

    private String existingFilesBasePath;
    private Collection<String> existingFiles;

    @Builder.Default
    private int objectsToGenerate = 1;

    public boolean shouldGenerateFiles() {
        return preparationMode == PreparationMode.GENERATE_FILES;
    }

    public boolean shouldUseExistingFiles() {
        return preparationMode == PreparationMode.FILES_EXIST;
    }
}
