import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {Experiment} from "../../../model/experiment";
import {ExperimentProgress} from "../../../model/experiment-progress";
import {BackendService} from "../../../services/backend.service";
import {ClientResultCount, ResultStatistic} from "../../../model/result-statistic";
import {ChartConfiguration, ChartOptions} from "chart.js";
import {ClientDescriptor} from "../../../model/client-descriptor";

@Component({
  selector: 'app-experiment-detail',
  templateUrl: './experiment-detail.component.html',
  styleUrls: ['./experiment-detail.component.scss']
})
export class ExperimentDetailComponent implements OnChanges {

  @Input() experiment: Experiment | null = null;
  @Input() clientId: string | null = null;

  @Input() visible = false;

  progress: ExperimentProgress | null = null;
  statistic: ResultStatistic | null = null;

  resultCounts: ClientResultCount[] = [];

  crashResultRegularPieChartOptions: ChartOptions<'pie'> = {
    responsive: true,
    interaction: {
      intersect: false,
      mode: 'index',
    },
    plugins: {
      legend: {
        labels: {
          color: 'white'
        },
        position: 'right'
      }
    }
  };
  crashResultRegularPieChartData: ChartConfiguration<'pie'>['data'] = {
    labels: ["Regular", "Hang", "Crash"],
    datasets: [
      {
        data: [],
        label: 'Result Statistic',
        backgroundColor: [
          'green',
          'yellow',
          'red'
        ],
        hoverOffset: 4
      }
    ]
  };

  private clients: ClientDescriptor[] = [];

  constructor(private backendService: BackendService) {
  }

  ngOnChanges(changes: SimpleChanges) {
    if(this.visible) {
      this.load();
    }
  }

  load() {
    if(this.experiment) {
      this.backendService.analysis.getProgress(this.experiment.id, this.clientId).subscribe(
        progress => this.progress = progress
      );

      this.backendService.analysis.getStatistic(this.experiment.id, this.clientId).subscribe(
        statistic => {
          this.crashResultRegularPieChartData.datasets[0].data =
            [statistic.regularResults, statistic.hangResults, statistic.crashResults]
          statistic.uniqueExceptions.sort((a, b) => b.count - a.count)
          this.statistic = statistic;
        }
      );

      this.backendService.clients.getClientDescriptors().subscribe(clients => this.clients = clients);

      if(this.clientId) {
        this.resultCounts = []
      } else {
        this.backendService.analysis.getClientsWithResultsOfExperiment(this.experiment.id).subscribe(
          results => this.resultCounts = results
        );
      }
    }
  }

  getClientNameForId(id: string): string {
    const client = this.clients.find(client => client.id === id);
    if(client) {
      return client.name
    } else {
      return id;
    }
  }

}
