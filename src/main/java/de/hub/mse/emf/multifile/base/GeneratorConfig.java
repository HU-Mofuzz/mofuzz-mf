package de.hub.mse.emf.multifile.base;

import lombok.*;

import java.util.Collection;

@Data
public class GeneratorConfig {

    private int filesToGenerate = 1;
    private int modelWidth = 10;
    private int modelDepth = 10;
    private double linkProbability = 0.5f;
    private double linkNumber = 0.5f;
    private PreparationMode preparationMode;

    private String workingDirectory;

    private String existingFilesBasePath;
    private Collection<String> existingFiles;

    private static GeneratorConfig instance;

    public static GeneratorConfig getInstance() {
        if (instance == null) {
            instance = new GeneratorConfig();
        }
        return instance;
    }

    private GeneratorConfig() {
    }

    public boolean shouldGenerateFiles() {
        return preparationMode == PreparationMode.GENERATE_FILES;
    }

    public boolean shouldUseExistingFiles() {
        return preparationMode == PreparationMode.FILES_EXIST;
    }

    public boolean shouldGenerateLinks() {
        return linkNumber > 0;
    }
    public double getLinkProbability() {
        return linkProbability > 0 ? linkProbability : Math.min(0.9f, linkNumber / 10.0f);
    }
}
