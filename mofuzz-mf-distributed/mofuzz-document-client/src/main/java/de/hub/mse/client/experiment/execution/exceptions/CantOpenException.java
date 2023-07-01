package de.hub.mse.client.experiment.execution.exceptions;

import java.io.File;

public class CantOpenException extends Exception {

    public CantOpenException(File file) {
        super("Unable to open "+file.getName());
    }
}
