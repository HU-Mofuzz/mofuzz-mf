package de.hub.mse.emf.multifile.impl.opendocument;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import de.hub.mse.emf.multifile.PoolBasedGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class XlsxGenerator extends PoolBasedGenerator<LinkedFile, String, XlsxSheetLink, XlsxGeneratorConfig> {

    private static final String XLSX_FILE_ENDING = ".xlsx";
    private static final int MAX_SHEET_COUNT = 5;
    private static final float CELL_FILL_CHANCE = 1.0f;

    private static final float CELL_LINK_CHANCE = 1.0f;

    public XlsxGenerator(XlsxGeneratorConfig config) {
        super(LinkedFile.class, config);
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

    private void addToLinkPool(File xlsxFile, String sheetId) {
        config.getLinkPool().add(new XlsxSheetLink(xlsxFile, sheetId));
    }

    private LinkedFile generateUnlinkedWorkbook(SourceOfRandomness random) {
        File xlsxFile = generateRandomXlsxFile();
        Set<String> sheets = new HashSet<>();
        try(XSSFWorkbook workbook = new XSSFWorkbook();
                OutputStream outputStream = new FileOutputStream(xlsxFile)) {
            workbook.setCellFormulaValidation(false);
            for(int i = 0; i < config.getSheetsPerDocument(); i++) {
                String sheetId = generateSheetId();
                Sheet sheet = workbook.createSheet(sheetId);
                fillSheet(sheet, random);
                sheets.add(sheetId);
            }
            workbook.write(outputStream);
            return new LinkedFile(xlsxFile, Collections.emptySet(), sheets, 0);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private LinkedFile generateLinkedWorkbook(SourceOfRandomness random, int depth) throws IOException {
        File xlsxFile = generateRandomXlsxFile();
        Set<File> linkedFiles = new HashSet<>();
        Set<String> sheets = new HashSet<>();
        try(XSSFWorkbook workbook = new XSSFWorkbook();
            OutputStream outputStream = new FileOutputStream(xlsxFile)) {
            workbook.setCellFormulaValidation(false);
            for(int i = 0; i < config.getSheetsPerDocument(); i++) {
                String sheetId = generateSheetId();
                Sheet sheet = workbook.createSheet(sheetId);
                linkedFiles.addAll(fillSheetWithLinks(sheet, random));
                sheets.add(sheetId);
            }
            workbook.write(outputStream);
            return new LinkedFile(xlsxFile, linkedFiles, sheets, depth);
        }
    }

    private void fillSheet(Sheet sheet, SourceOfRandomness random) {
        for(int rowNumber = 0; rowNumber < config.getModelHeight(); rowNumber++) {
            Row row = sheet.createRow(rowNumber);
            for(int columnNumber = 0; columnNumber < config.getModelWidth(); columnNumber++) {
                if(random.nextFloat() < CELL_FILL_CHANCE) {
                    Cell cell = row.createCell(columnNumber);
                    fillCell(cell, random);
                }
            }
        }
    }

    private Set<File> fillSheetWithLinks(Sheet sheet, SourceOfRandomness random) {
        Set<File> linkedFiles = new HashSet<>();
        for(int rowNumber = 0; rowNumber < config.getModelHeight(); rowNumber++) {
            Row row = sheet.createRow(rowNumber);
            for(int columnNumber = 0; columnNumber < config.getModelWidth(); columnNumber++) {
                if(random.nextFloat() < CELL_FILL_CHANCE) {
                    Cell cell = row.createCell(columnNumber);
                    if(random.nextFloat() < CELL_LINK_CHANCE) {
                        var link = random.choose(config.getLinkPool());
                        cell.setCellFormula(link
                                .toExcelCellReferenceFormula(random.nextInt(1, config.getModelHeight()),
                                        random.nextInt(0, config.getModelWidth())));
                        linkedFiles.add(link.getFile());
                    } else {
                        fillCell(cell, random);
                    }
                }
            }
        }
        return linkedFiles;
    }

    @Override
    public File getWorkingDirFileForId(String name) {
        return Paths.get(config.getWorkingDirectory(), name + XLSX_FILE_ENDING).toFile();
    }

    private File generateRandomXlsxFile() {
        return getWorkingDirFileForId(UUID.randomUUID().toString());
    }

    @Override
    public Collection<LinkedFile> prepareLinkPool(SourceOfRandomness random) {
        List<LinkedFile> resultingFiles = new ArrayList<>();

        long filesPerLevel = Math.round(Math.sqrt(
                config.getModelHeight() * config.getModelWidth()
                        * config.getSheetsPerDocument() * config.getTargetDocumentDepth()
        ));
        log.info("Preparing link pool  with {} files per level at {} levels", filesPerLevel, config.getTargetDocumentDepth() - 1);

        for (int level = 0; level < config.getTargetDocumentDepth(); level++) {
            log.info("Start to generate level {}", level);
            Set<LinkedFile> filesOfLevel = new HashSet<>();
            for(int i = 0; i < filesPerLevel; i++) {
                LinkedFile file = null;
                try {
                    if(level == 0) {
                        file = generateUnlinkedWorkbook(random);
                    } else {
                        file = generateLinkedWorkbook(random, level);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(file != null) {
                    filesOfLevel.add(file);
                }
                if(i > 0 && i % 10 == 0) {
                    log.info("Generation progress - Level {} - {}/{}", level, i, filesPerLevel);
                }
            }

            // always add whole levels to the link pool to ensure depth
            for(var file : filesOfLevel) {
                for(var sheetId : file.getSheets()) {
                    config.getLinkPool().add(new XlsxSheetLink(file.getMainFile(), sheetId));
                }
            }
            resultingFiles.addAll(filesOfLevel);
        }
        return resultingFiles;
    }

    @Override
    public LinkedFile internalExecute(SourceOfRandomness random) throws Exception {
        return generateLinkedWorkbook(random, config.getTargetDocumentDepth());
    }

    @Override
    public void addLinkFromSerialized(String serialized) {
        XlsxSheetLink link = new XlsxSheetLink();
        link.deserializeLink(config.workingDirectory, serialized);
        config.getLinkPool().add(link);
    }
}
