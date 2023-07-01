package de.hub.mse.client.experiment.execution;

import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.DisposedException;
import com.sun.star.sheet.XSpreadsheetDocument;
import de.hub.mse.client.experiment.execution.exceptions.CantOpenException;
import de.hub.mse.office.CustomBootstrap;
import helper.Calc;
import helper.Lo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class LibreOfficeCalc implements Application {

    public static final String LIBRE_OFFICE_ID = "LibreOffice";

    private XComponentLoader componentLoader = null;

    @Override
    public boolean prepare() throws Exception {
        componentLoader = CustomBootstrap.bootstrap();
        if(componentLoader == null) {
            throw new IllegalStateException("Unable to create component loader!");
        }
        return true;
    }

    @Override
    public boolean isExecutionPrepared() {
        return componentLoader != null;
    }

    @Override
    public int execute(File file, int height, int width, InterruptionHook hook) throws Exception {
        if(!isExecutionPrepared()) {
            throw new IllegalStateException("ComponentLoader not prepared!");
        }

        try(var doc = new ClosableSpreadsheet(Calc.openDoc(file.getAbsolutePath(), componentLoader))) {
            if (doc.delegate == null) {
                log.error("Could not open {}", file.getName());
                throw new CantOpenException(file);
            }
            log.info("Opened file: "+file.getName());
            int errorCount = 0;

            for(var sheetName : doc.delegate.getSheets().getElementNames()) {
                log.info("Looking at sheet "+sheetName);
                var sheet = Calc.getSheet(doc.delegate, sheetName);
                Calc.setActiveSheet(doc.delegate, sheet);
                for(int rowNum = 0; rowNum < height; rowNum++) {
                    for (int columnNum = 0; columnNum < width; columnNum++) {
                        var cell = Calc.getCell(sheet, columnNum, rowNum);

                        if(cell.getError() != 0) {
                            errorCount++;
                        }

                        if(hook.isRaised()) {
                            hook.accept();
                            return 0;
                        }
                    }
                }
            }
            Lo.closeDoc(doc);
            log.info("Closed file: "+file.getName());
            return errorCount;
        }
    }

    @Override
    public void cleanup() {
        Lo.tryToTerminate(3);
    }

    @Override
    public boolean shouldRetry(Exception exception) {
        return exception instanceof DisposedException;
    }

    @AllArgsConstructor
    private static class ClosableSpreadsheet implements AutoCloseable {

        private final XSpreadsheetDocument delegate;
        @Override
        public void close() throws Exception {
            if(delegate != null) {
                Lo.closeDoc(delegate);
            }
        }
    }
}
