package de.hub.mse.emf.multifile.base;

import java.util.Collection;

public interface IGeneratorConfig {

    String getWorkingDirectory();

    int getFilesToGenerate();

    PreparationMode getPreparationMode();

    default boolean shouldGenerateFiles() {
        return getPreparationMode() == PreparationMode.GENERATE_FILES;
    }

    default boolean shouldUseExistingFiles() {
        return getPreparationMode() == PreparationMode.FILES_EXIST;
    }

    public Collection<String> getExistingFiles();
    public void setExistingFiles(Collection<String> files);
}
