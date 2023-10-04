package de.hub.mse.server.service.analysis;

import de.hub.mse.emf.multifile.impl.opendocument.XlsxGenerator;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FilenameUtil {

    public String mapFileKey(String key, String mainFile) {
        if(mainFile.equals(key)) {
            return "Main_" + key + XlsxGenerator.XLSX_FILE_ENDING;
        } else {
            return key + XlsxGenerator.XLSX_FILE_ENDING;
        }
    }
}
