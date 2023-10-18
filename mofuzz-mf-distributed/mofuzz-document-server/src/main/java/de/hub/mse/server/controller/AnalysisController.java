package de.hub.mse.server.controller;

import de.hub.mse.server.management.ExecutionResult;
import de.hub.mse.server.management.Experiment;
import de.hub.mse.server.service.AnalysisService;
import de.hub.mse.server.service.ExperimentService;
import de.hub.mse.server.service.analysis.ClientResultCount;
import de.hub.mse.server.service.analysis.ExperimentProgress;
import de.hub.mse.server.service.analysis.PageResponse;
import de.hub.mse.server.service.analysis.ResultStatistic;
import de.hub.mse.server.service.analysis.data.ExperimentHealthData;
import de.hub.mse.server.service.analysis.data.ResearchQuestionData;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;

@Slf4j
@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final ExperimentService experimentService;

    @Autowired
    public AnalysisController(AnalysisService analysisService, ExperimentService experimentService) {
        this.analysisService = analysisService;
        this.experimentService = experimentService;
    }

    @GetMapping("/progress/{id}")
    public ExperimentProgress getProgress(@PathVariable String id, @RequestParam(required = false) String client) {
        int results;
        if(client == null || client.isEmpty()) {
            results = analysisService.getResultCountForExperiment(id);
        } else {
            results = analysisService.getResultCountForClient(id, client);
        }
        return new ExperimentProgress(results, analysisService.getGeneratedDocumentCount(id),
                experimentService.getExperiment(id).map(Experiment::getDocumentCount).orElse(0));
    }

    @GetMapping("/statistic/{id}")
    public ResultStatistic getStatistics(@PathVariable String id, @RequestParam(required = false) String client) {
        if(client == null || client.isEmpty()) {
            return analysisService.getStatisticForExperiment(id);
        } else {
            return analysisService.getStatisticForClient(id, client);
        }
    }

    @GetMapping("/results/{id}")
    public PageResponse<ExecutionResult> getResults(@PathVariable String id,
                                                    @RequestParam(required = false) String client,
                                                    @RequestParam(required = false) String sort,
                                                    @RequestParam(required = false) String order,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        if(client == null || client.isEmpty()) {
            return analysisService.getResults(id, sort, order, page, pageSize);
        } else {
            return analysisService.getResultsForClient(id, client, sort, order, page, pageSize);
        }
    }

    @GetMapping("/clients/{id}")
    public List<ClientResultCount> getClientsWithResultsOfExperiment(@PathVariable String id) {
        return analysisService.getClientsWithResultsOfExperiment(id);
    }

    @GetMapping("/health/{id}")
    public ExperimentHealthData getHealthData(@PathVariable String id,
                                              @RequestParam String client,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "25") int pageSize) {
        return analysisService.getDataForExperiment(id, client, page, pageSize);
    }

    @GetMapping("/fileTree/{id}")
    public void getFileTreeForFileDescriptor(@PathVariable String id,
                                             HttpServletResponse response) {
        try(ZipOutputStream outputStream = new ZipOutputStream(response.getOutputStream())) {

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment;filename=%s", id+"_files.zip"));
            response.setStatus(HttpServletResponse.SC_OK);

            analysisService.zipFileTreeOfDescriptor(id, outputStream);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/researchQuestionData")
    public ResearchQuestionData getResearchQuestionData() {
        log.info("Starting to gather research question data...");
        var start = System.currentTimeMillis();
        var data = analysisService.getResearchQuestionData();
        log.info("Gathered Research Data, took {} ms", (System.currentTimeMillis() - start));
        return data;
    }
}
