package de.hub.mse.emf.multifile.impl.opendocument;

import de.hub.mse.emf.multifile.SerializableLink;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.nio.file.Paths;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XlsxSheetLink implements SerializableLink<String> {

    /*
         Note that sheet name is Excel must not exceed 31 characters
         and must not contain any of the following characters:
         0x0000
         0x0003
         colon (:)
         backslash (\)
         asterisk (*)
         question mark (?)
         forward slash (/)
         opening square bracket ([)
         closing square bracket (])

         So they are free to use as serialization delimiter
        */
    private static final String SERIALIZATION_DELIMITER = ":";

    private File file;
    private String sheetName;

    private static String columnToIdentifier(int column) {
        if(column < 0) {
            return "";
        } else if(column < 26) {
            return "" + (char)('A'+column);
        } else {
            StringBuilder result = new StringBuilder();
            while (--column >= 0) {
                result.append((char)('A' + (column % 25)));
                column /= 25;
            }
            return result.reverse().toString();
        }
    }

    public String toExcelCellReferenceFormula(int row, int column) {
        return String.format("'[%s]%s'!$%s$%d", file.getName(), sheetName,
                columnToIdentifier(column), row);
    }

    public String toOpenOfficeCellReferenceFormula(int row, int column) {
        return String.format("'file:///home/laokoon/tmp/linktest/%s'#$'%s'.%s%d", file.getName(), sheetName,
                columnToIdentifier(column), row);
    }

    @Override
    public String serializeLink() {
        return file.getName() + SERIALIZATION_DELIMITER + sheetName;
    }

    @Override
    public void deserializeLink(String workingDirectory, String serialized) {
        String[] parts = serialized.split(SERIALIZATION_DELIMITER);
        if(parts.length != 2) {
            throw new IllegalStateException("Exception while deserializing link: "+serialized);
        }
        this.file = Paths.get(workingDirectory, parts[0]).toFile();
        this.sheetName = parts[1];
    }
}
