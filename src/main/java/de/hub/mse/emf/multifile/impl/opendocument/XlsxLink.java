package de.hub.mse.emf.multifile.impl.opendocument;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

@Data
@AllArgsConstructor
public class XlsxLink {

    private final File file;
    private final String sheetName;
    private final int row;
    private final int column;

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

    public String toExcelCellReferenceFormula() {
        return String.format("'[%s]%s'!$%s$%d", file.getName(), sheetName,
                columnToIdentifier(column), row);
    }

    public String toOpenOfficeCellReferenceFormula() {
        return String.format("'file:///home/laokoon/tmp/linktest/%s'#$'%s'.%s%d", file.getName(), sheetName,
                columnToIdentifier(column), row);
    }
}
