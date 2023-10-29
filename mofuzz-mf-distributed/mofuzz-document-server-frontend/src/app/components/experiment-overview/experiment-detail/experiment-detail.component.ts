import {Component, Input, OnChanges, QueryList, SimpleChanges, ViewChild, ViewChildren} from '@angular/core';
import {Experiment} from "../../../model/experiment";
import {ExperimentProgress} from "../../../model/experiment-progress";
import {BackendService} from "../../../services/backend.service";
import {ClientResultCount, ResultStatistic} from "../../../model/result-statistic";
import {ChartConfiguration, ChartOptions} from "chart.js";
import {ClientDescriptor} from "../../../model/client-descriptor";
import {ExperimentHealthData} from "../../../model/experiment-health-data";
import {
  INDEX_CPU,
  INDEX_DISK,
  INDEX_RAM,
  INDEX_RESULTS,
  transformHealthData
} from "../../../utils/data-transform";
import {formatDate} from "@angular/common";
import {PageEvent} from "@angular/material/paginator";
import {BaseChartDirective} from "ng2-charts";

@Component({
  selector: 'app-experiment-detail',
  templateUrl: './experiment-detail.component.html',
  styleUrls: ['./experiment-detail.component.scss']
})
export class ExperimentDetailComponent implements OnChanges {

  readonly PAGE_SIZE_OPTIONS = [25, 50, 100, 150, 200];

  @Input() experiment: Experiment | null = null;
  @Input() clientId: string | null = null;

  @Input() visible = false;

  @ViewChildren(BaseChartDirective) charts: QueryList<BaseChartDirective> | null = null;

  progress: ExperimentProgress | null = null;
  statistic: ResultStatistic | null = null;

  resultCounts: ClientResultCount[] = [];

  healthData: ExperimentHealthData | null = null;

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

  healthChartOptions: ChartOptions<'line'> = {
    responsive: true,
    aspectRatio: 5,
    spanGaps: true,
    scales: {
      y: {
        min: 0,
        max: 100,
      },
      resultScale: {
        position: 'right', // `axis` is determined by the position as `'y'`
      }
    },
    interaction: {
      intersect: false,
      mode: 'index',
    },
    animation: false
  };
  healthChartData:  ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'CPU',
        fill: false,
        tension: 0.3,
        borderColor: 'red',
        pointBackgroundColor: 'red',
        backgroundColor: 'rgba(255,0,0,0.1)'
      },
      {
        data: [],
        label: 'Memory',
        fill: false,
        tension: 0.3,
        borderColor: 'green',
        pointBackgroundColor: 'green',
        backgroundColor: 'rgba(0,255,0,0.1)'
      },
      {
        data: [],
        label: 'Disk',
        fill: false,
        tension: 0.3,
        borderColor: 'blue',
        pointBackgroundColor: 'blue',
        backgroundColor: 'rgba(0,0,255,0.1)'
      },
      {
        data: [],
        label: 'Result Count',
        fill: false,
        tension: 0,
        borderColor: 'grey',
        pointBackgroundColor: 'grey',
        backgroundColor: 'rgba(255,255,255,0.1)',
        stepped: 'before',
        spanGaps: true,
        yAxisID: "resultScale"
      }
    ]
  };

  pageSize = this.PAGE_SIZE_OPTIONS[0]
  page = 0;
  totalItemCount = 0;

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
        this.updateChartData();
      } else {
        this.healthData = null;
        this.backendService.analysis.getClientsWithResultsOfExperiment(this.experiment.id).subscribe(
          results => this.resultCounts = results
        );
      }
    }
  }

  getClientNameForId(id: string | null): string | null {
    const client = this.clients.find(client => client.id === id);
    if(client) {
      return client.name
    } else {
      return id;
    }
  }

  pageChanged(event: PageEvent) {
    if (event.pageSize === this.pageSize) {
      this.page = event.pageIndex;
    } else {
      this.page = 0
    }
    this.pageSize = event.pageSize
    this.updateChartData()
  }

  private setChartData(result: {labels: (number | null)[], data: (number | null)[][]}) {
    this.healthChartData.datasets[INDEX_CPU].data = [...result.data[INDEX_CPU]];
    this.healthChartData.datasets[INDEX_RAM].data = [...result.data[INDEX_RAM]];
    this.healthChartData.datasets[INDEX_DISK].data = [...result.data[INDEX_DISK]];
    this.healthChartData.datasets[INDEX_RESULTS].data = [...result.data[INDEX_RESULTS]];

    const format = 'HH:mm:ss';
    const locale = 'en-US';
    this.healthChartData.labels = result.labels.map(label => label ? formatDate(label, format, locale): null);

    if(this.charts) {
      this.charts.forEach(chart => chart.update());
    }
  }

  private updateChartData() {
    if(!this.experiment || !this.clientId) {
      return
    }
    this.backendService.analysis.getHealthData(this.experiment.id, this.clientId, this.page, this.pageSize).subscribe(
      healthData => {
        this.totalItemCount = healthData.totalElements;
        if (typeof Worker !== 'undefined') {
          // Create a new
          const worker = new Worker(new URL('../../../worker/health-transformer.worker', import.meta.url));
          worker.onmessage = ({ data }) => {
            this.setChartData(data)
            this.healthData = healthData;
          };
          worker.postMessage(healthData);
        } else {
          // Web Workers are not supported in this environment.
          const result = transformHealthData(healthData);
          this.healthData = healthData;
          this.setChartData(result);
        }
      }
    )
  }

  downloadChart(chart: HTMLCanvasElement, hint = "chart", clientName: string | null = null) {
    const link = document.createElement('a');
    link.href = chart.toDataURL("image/png");

    let filename = this.experiment?.description.replaceAll(" ", "_").replaceAll(".", "")
                  + "_" + hint.toLowerCase()
                  + (clientName ? "_" + clientName.replaceAll(" ", "_").replaceAll(".", "") : '')
                  + ".png"
    link.download = filename;
    link.click();
  }
}
