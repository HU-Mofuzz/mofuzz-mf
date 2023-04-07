package de.hub.mse.emf.multifile.impl.opendocument;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.base.AbstractGenerator;
import de.hub.mse.emf.multifile.base.GeneratorConfig;
import de.hub.mse.emf.multifile.base.LinkPool;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.*;

public class XlsxGenerator extends AbstractGenerator<File, XlsxLink, GeneratorConfig> {

    private static final String XLSX_FILE_ENDING = ".xlsx";
    private static final int MAX_SHEET_COUNT = 5;
    private static final float CELL_FILL_CHANCE = 1.0f;

    private static final float CELL_LINK_CHANCE = 1.0f;

    public XlsxGenerator() {
        super(File.class, GeneratorConfig.getInstance());
    }

    private static String generateSheetId() {
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
        */
        return UUID.randomUUID().toString()
                .substring(0, 31);
    }

    private static int getRandomSheetCount() {
        return Math.max(1, (int)Math.round(Math.random() * MAX_SHEET_COUNT));
    }

    private static void fillCell(Cell cell, SourceOfRandomness random) {
        switch(random.nextByte((byte) 1, (byte) 7)) {
            case 1:
                cell.setCellFormula(RandomStringUtils.random(random.nextInt(32)));
                break;
            case 2:
                cell.setCellValue(new Date(random.nextLong()));
                break;
            case 3:
                cell.setCellValue(random.nextDouble());
                break;
            case 4:
                cell.setCellValue(RandomStringUtils.random(random.nextInt(32)));
                break;
            case 5:
                cell.setCellValue(random.nextBoolean());
                break;
            case 6:
                var text = new HSSFRichTextString(RandomStringUtils.random(random.nextInt(32)));
                // text.applyFont();
                cell.setCellValue(text);
                break;
            case 7:
            default:
                cell.setBlank();
                break;
        }
    }

    private Collection<XlsxLink> generateUnlinkedWorkbook(SourceOfRandomness random) {
        File xlsxFile = generateRandomXlsxFile();
        try(XSSFWorkbook workbook = new XSSFWorkbook();
                OutputStream outputStream = new FileOutputStream(xlsxFile)) {
            workbook.setCellFormulaValidation(false);
            Set<XlsxLink> links = new HashSet<>();
            for(int i = 0; i < getRandomSheetCount(); i++) {
                String sheetId = generateSheetId();
                Sheet sheet = workbook.createSheet(sheetId);
                fillSheet(sheet, random);
                for(int row = 0; row < config.getModelDepth(); row++) {
                    for(int column = 0; column < config.getModelWidth(); column++) {
                        links.add(new XlsxLink(xlsxFile, sheetId, (row+1), column));
                    }
                }
            }
            workbook.write(outputStream);
            return links;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private void fillSheet(Sheet sheet, SourceOfRandomness random) {
        for(int rowNumber = 0; rowNumber < config.getModelDepth(); rowNumber++) {
            Row row = sheet.createRow(rowNumber);
            for(int columnNumber = 0; columnNumber < config.getModelWidth(); columnNumber++) {
                if(random.nextFloat() < CELL_FILL_CHANCE) {
                    Cell cell = row.createCell(columnNumber);
                    fillCell(cell, random);
                }
            }
        }
    }

    private void fillSheetWithLinks(Sheet sheet, SourceOfRandomness random) {
        for(int rowNumber = 0; rowNumber < config.getModelDepth(); rowNumber++) {
            Row row = sheet.createRow(rowNumber);
            for(int columnNumber = 0; columnNumber < config.getModelWidth(); columnNumber++) {
                if(random.nextFloat() < CELL_FILL_CHANCE) {
                    Cell cell = row.createCell(columnNumber);
                    if(random.nextFloat() < CELL_LINK_CHANCE) {
                        cell.setCellFormula(random.choose(getLinkPool())
                                .toExcelCellReferenceFormula());
                    } else {
                        fillCell(cell, random);
                    }
                }
            }
        }
    }

    private File generateRandomXlsxFile() {
        var fileId = UUID.randomUUID().toString();
        return Paths.get(config.getWorkingDirectory(), fileId + XLSX_FILE_ENDING).toFile();
    }

    @Override
    protected LinkPool<XlsxLink> collectLinksFromConfig(SourceOfRandomness random) {
        LinkPool<XlsxLink> pool = new LinkPool<>();
        for (int i = 0; i < config.getFilesToGenerate(); i++) {
            pool.addAll(generateUnlinkedWorkbook(random));
        }
        return pool;
    }

    @Override
    public File internalExecute(SourceOfRandomness random, LinkPool<XlsxLink> linkPool) throws Exception {
        File xlsxFile = generateRandomXlsxFile();
        try(XSSFWorkbook workbook = new XSSFWorkbook();
            OutputStream outputStream = new FileOutputStream(xlsxFile)) {
            workbook.setCellFormulaValidation(false);
            for(int i = 0; i < getRandomSheetCount(); i++) {
                String sheetId = generateSheetId();
                Sheet sheet = workbook.createSheet(sheetId);
                fillSheetWithLinks(sheet, random);
            }
            workbook.write(outputStream);
            return xlsxFile;
        }
    }
}
