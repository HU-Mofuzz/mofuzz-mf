package de.hub.mse.emf.multifile.impl.opendocument;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.base.AbstractGenerator;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.PreparationMode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.stream.Collectors;

public class ExcelTest {

    private static String columnToExcelIdentifier(int column) {
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

    @Test
    public void testColumnToExcelIdentifier() {
        Assert.assertEquals("A", columnToExcelIdentifier(0));
        Assert.assertEquals("Z", columnToExcelIdentifier(25));
        Assert.assertEquals("AA", columnToExcelIdentifier(26));
        Assert.assertEquals("AB", columnToExcelIdentifier(27));
        Assert.assertEquals("BA", columnToExcelIdentifier(51));
        Assert.assertEquals("BC", columnToExcelIdentifier(53));
        Assert.assertEquals("CA", columnToExcelIdentifier(76));
        Assert.assertEquals("CE", columnToExcelIdentifier(80));
    }

    @Test
    public void testGeneratorManually() {
        var config = GeneratorConfig.getInstance();

        config.setWorkingDirectory("/home/laokoon/tmp/linktest");
        config.setPreparationMode(PreparationMode.GENERATE_FILES);
        config.setFilesToGenerate(2);
        config.setModelDepth(10);
        config.setModelWidth(10);

        XlsxGenerator generator = new XlsxGenerator();
        SourceOfRandomness random = new SourceOfRandomness(new Random());

        var file = generator.generate(random, null);
        Assert.assertNotNull(file);
        System.out.println("Link-Pool: "+generator.getLinkPool().stream()
                .map(link -> link.getFile().getName())
                .distinct()
                .collect(Collectors.joining(", ")));
        System.out.println("Result: "+file.getName());
    }

    @Test
    public void readCells() throws IOException {
        String sheetId = "a41c852a-906e-41f6-abc2-1faedcf";

        try(FileInputStream file = new FileInputStream(new File("/home/laokoon/tmp/linktest/0f2ac2b4-798d-4979-81ba-453032b901a0.xlsx "))) {
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            Iterator<Sheet> sheetIterator = workbook.sheetIterator();
            while(sheetIterator.hasNext()) {
                Sheet sheet = sheetIterator.next();
                System.out.println("=== SHEET ["+sheet.getSheetName()+"] ===\n");
                //Iterate through each rows one by one
                Iterator<Row> rowIterator = sheet.iterator();
                int rowNum = 1;
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    //For each row, iterate through all the columns
                    Iterator<Cell> cellIterator = row.cellIterator();
                    int columnNum = 0;
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        //Check the cell type and format accordingly
                        String value;
                        switch (cell.getCellType()) {
                            case NUMERIC:
                                value = Double.toString(cell.getNumericCellValue());
                                break;
                            case STRING:
                                value = cell.getStringCellValue();
                                break;
                            case FORMULA:
                                value = cell.getCellFormula();
                                break;
                            case BLANK:
                                value = "<Blank>";
                                break;
                            case BOOLEAN:
                                value = cell.getBooleanCellValue() ? "<TRUE>" : "<FALSE>";
                                break;
                            case ERROR:
                                value = "<ERROR: " + cell.getErrorCellValue() + ">";
                                break;
                            default:
                                value = "UNKNOWN";
                                break;
                        }
                        System.out.println("["+columnToExcelIdentifier(columnNum++)+rowNum+"|"+cell.getCellType().name()+"] "+value);
                    }
                    rowNum++;
                }
                System.out.println("\n");
            }
        }
    }
}
