package de.hub.mse.server.controller;

import de.hub.mse.server.management.ExecutionResult;
import de.hub.mse.server.management.Experiment;
import de.hub.mse.server.service.AnalysisService;
import de.hub.mse.server.service.ExperimentService;
import de.hub.mse.server.service.analysis.ClientResultCount;
import de.hub.mse.server.service.analysis.ExperimentProgress;
import de.hub.mse.server.service.analysis.PageResponse;
import de.hub.mse.server.service.analysis.ResultStatistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
