package de.hub.mse.emf.multifile;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import lombok.Getter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

/**
 * Abstract Generator for multi document generation
 * @param <D> The target document type, e.g. {@link org.eclipse.emf.ecore.resource.Resource} or {@link String} for a file path
 * @param <L> The Link type between multiple documents, e.g. {@link String} in SVG/XML xlink hrefs
 * @param <C> The configuration type for the generation and preparation
 */
public abstract class PreparingGenerator<D, L, C extends PreparingGeneratorConfig> extends Generator<D> {

    protected final C config;
    private boolean prepared = false;

    @Getter
    private LinkPool<L> linkPool;

    protected PreparingGenerator(Class<D> type, C config) {
        super(type);
        this.config = config;
    }

    protected abstract LinkPool<L> collectLinksFromConfig(SourceOfRandomness sourceOfRandomness);
    protected abstract D internalExecute(SourceOfRandomness sourceOfRandomness, LinkPool<L> linkPool) throws Exception;

    @Override
    public D generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
        if(!prepared) {
            // ensure working directory
            File workDir = Paths.get(config.getWorkingDirectory()).toFile();
            if(!workDir.exists() && !workDir.mkdirs()) {
                throw new IllegalStateException("Could not create working directory: " +
                        config.getWorkingDirectory());
            }

            if(config.shouldUseExistingFiles()) {
                // copy predefined files from config

                config.setExistingFiles(
                        config.getExistingFiles().stream()
                                .filter(path -> {
                                    try {
                                        var src = Paths.get(path);
                                        if(src.getParent() == null) {
                                            src = Paths.get(config.getWorkingDirectory(), path);
                                        }
                                        var dst = Paths.get(config.getWorkingDirectory());
                                        System.out.println(src + " -> " + dst);
                                        if(!src.getParent().equals(dst)) {
                                            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                                        }
                                        return true;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        return false;
                                    }
                                })
                                .map(path -> Paths.get(path).toFile().getName())
                                .peek(System.out::println)
                                .collect(Collectors.toList()));
            }
            linkPool = collectLinksFromConfig(sourceOfRandomness);
            if(linkPool.isEmpty()) {
                throw new IllegalStateException("No links to reference!");
            }
            prepared = true;
        }
        try {
            return internalExecute(sourceOfRandomness, linkPool);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
