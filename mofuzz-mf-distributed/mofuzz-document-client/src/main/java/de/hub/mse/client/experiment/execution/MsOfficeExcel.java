package de.hub.mse.client.experiment.execution;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.office.MSExcel;
import com.sun.jna.platform.win32.Ole32;
import de.hub.mse.client.experiment.execution.exceptions.CantOpenException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class MsOfficeExcel implements Application {

    private boolean initialized = false;

    public static final String MS_OFFICE_ID = "MSOffice";
    private MSExcel excel = null;

    @Override
    public boolean prepare() throws Exception {
        if(!initialized) {
            initialized = true;
            Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
        }
        excel = new MSExcel();
        excel.setVisible(true);
        excel.disableAskUpdateLinks();
        return true;
    }

    @Override
    public boolean isExecutionPrepared() {
        return excel != null;
    }

    @Override
    public int execute(File file, int height, int width, InterruptionHook hook) throws Exception {
        if(!isExecutionPrepared()) {
            throw new IllegalStateException("Excel not prepared!");
        }

        excel.openExcelBook(file.getAbsolutePath());
        MSExcel.Workbook workbook = excel.getActiveWorkbook();

        if (workbook == null) {
            log.error("Could not open {}", file.getName());
            throw new CantOpenException(file);
        }
        log.info("Opened file: "+file.getName());
        MSExcel.Sheets sheets = workbook.getSheets();
        int errorCount = 0;
        // SHEETS ARE ONE BASED!!!
        for (int sheetIndex = 1; sheetIndex <= sheets.size(); sheetIndex++) {
            MSExcel.Sheet sheet = sheets.getSheet(sheetIndex);
            System.out.println("Looking at sheet "+sheet.name());
            MSExcel.Range cells = sheet.cells();
            for (int rowNum = 0; rowNum < height; rowNum++) {
                for (int columnNum = 0; columnNum < width; columnNum++) {
                    String cellIdentifier = MSExcel.columnNumberStr(columnNum) + (rowNum+1);
                    MSExcel.Range cell = cells.getRange(cellIdentifier);
                    if(cell.isError()) {
                        errorCount++;
                    }

                    if(hook.isRaised()) {
                        hook.accept();
                        return 0;
                    }
                }
            }
        }
        excel.closeActiveWorkbook(false);
        log.info("Closed file: "+file.getName());
        return errorCount;
    }

    @Override
    public boolean shouldRetry(Exception exception) {
        return false;
    }

    @Override
    public void cleanup() {
        for (int i = 0; i < excel.getWorkbooks().count(); i++) {
            excel.closeActiveWorkbook(false);
        }
        excel.quit();
        excel = null;
    }
}
