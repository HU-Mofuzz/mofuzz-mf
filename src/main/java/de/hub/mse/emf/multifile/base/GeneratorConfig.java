package de.hub.mse.emf.multifile.base;

import lombok.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

@Data
public class GeneratorConfig {

    private int filesToGenerate = 1;
    private int modelWidth = 4;
    private int modelDepth = 4;
    private double linkProbability = 0.5f;
    private int linkNumber = 1;
    private PreparationMode preparationMode = PreparationMode.GENERATE_FILES;

    private String workingDirectory = "svg_test";

    @SneakyThrows
    public String getWorkingDirectory() {
        if (!Files.exists(Path.of(workingDirectory))) {
           workingDirectory = Files.createTempDirectory(workingDirectory).toFile().getAbsolutePath() ;
        }
        return workingDirectory;
    }

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
