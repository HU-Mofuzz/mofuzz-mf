package de.hub.mse.emf.multifile.impl.opendocument;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.util.Set;

@Data
@AllArgsConstructor
public class LinkedFile {

    private final File mainFile;

    private final Set<File> linkedFiles;

    private final Set<String> sheets;

    private final int depth;

}
