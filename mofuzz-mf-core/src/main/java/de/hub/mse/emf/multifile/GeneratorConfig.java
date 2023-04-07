package de.hub.mse.emf.multifile;

import lombok.*;

import java.util.Collection;

@Data
public class GeneratorConfig implements IGeneratorConfig {

    private int filesToGenerate = 1;
    private int modelWidth = 10;
    private int modelDepth = 10;

    private PreparationMode preparationMode;

    private String workingDirectory;

    private String existingFilesBasePath;
    private Collection<String> existingFiles;

    private static GeneratorConfig instance;

    public static GeneratorConfig getInstance() {
        if(instance == null) {
            instance = new GeneratorConfig();
        }
        return instance;
    }

    private GeneratorConfig() {}
}
