package de.hub.mse.client.util;

import de.hub.mse.client.experiment.execution.Application;
import de.hub.mse.client.experiment.execution.LibreOfficeCalc;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApplicationUtil {

    public Application getApplicationForType(String type) {
        if(LibreOfficeCalc.LIBRE_OFFICE_ID.equals(type)) {
            return new LibreOfficeCalc();
        }
        throw new IllegalStateException("Can't determine application for client type: "+type);
    }
}
