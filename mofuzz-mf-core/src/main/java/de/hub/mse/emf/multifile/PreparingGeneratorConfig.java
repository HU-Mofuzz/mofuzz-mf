package de.hub.mse.emf.multifile;

import java.util.Collection;

public interface PreparingGeneratorConfig extends IGeneratorConfig {

    int getFilesToGenerate();

    PreparationMode getPreparationMode();

    default boolean shouldGenerateFiles() {
        return getPreparationMode() == PreparationMode.GENERATE_FILES;
    }

    default boolean shouldUseExistingFiles() {
        return getPreparationMode() == PreparationMode.FILES_EXIST;
    }

    Collection<String> getExistingFiles();
    void setExistingFiles(Collection<String> files);
}
