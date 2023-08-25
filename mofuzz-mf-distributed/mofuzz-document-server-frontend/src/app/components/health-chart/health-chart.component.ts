import {Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {HEALTH_TOPIC, SocketService} from "../../services/socket.service";
import {Subscription} from "rxjs";
import {HealthSnapshot} from "../../model/health-snapshot";
import {ChartConfiguration, ChartOptions} from "chart.js";
import {BaseChartDirective} from "ng2-charts";
import {formatDate} from "@angular/common";

const INDEX_CPU = 0;
const INDEX_RAM = 1;
const INDEX_DISK = 2;

const CHART_LENGTH = 30;

@Component({
  selector: 'app-health-chart',
  templateUrl: './health-chart.component.html',
  styleUrls: ['./health-chart.component.scss']
})
export class HealthChartComponent implements OnChanges, OnDestroy {

  readonly ENOUGH_DATA = 2;

  @Input()
  system: string = '';

  @ViewChild(BaseChartDirective) baseChart: BaseChartDirective | null = null;

  topicSubscription = new Subscription();
  lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    aspectRatio: 5,
    scales: {
      y: {
        min: 0,
        max: 100,
      }
    },
    interaction: {
      intersect: false,
      mode: 'index',
    }
  };
  lineChartData:  ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'CPU',
        fill: true,
        tension: 0.3,
        borderColor: 'red',
        pointBackgroundColor: 'red',
        backgroundColor: 'rgba(255,0,0,0.1)'
      },
      {
        data: [],
        label: 'Memory',
        fill: true,
        tension: 0.3,
        borderColor: 'green',
        pointBackgroundColor: 'green',
        backgroundColor: 'rgba(0,255,0,0.1)'
      },
      {
        data: [],
        label: 'Disk',
        fill: true,
        tension: 0.3,
        borderColor: 'blue',
        pointBackgroundColor: 'blue',
        backgroundColor: 'rgba(0,0,255,0.1)'
      }
    ]
  };

  dataCount = 0;

  private static trimArray(arr: any[]) {
    while (arr.length > CHART_LENGTH) {
      arr.splice(0, 1);
    }
  }

  constructor(private socketService: SocketService) {
    for(let i = 0; i < CHART_LENGTH; i++) {
      this.lineChartData.labels?.push("")
    }
  }

  ngOnDestroy(): void {
    this.topicSubscription.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if(this.system.length > 0) {
      this.topicSubscription.unsubscribe();
      this.topicSubscription = this.socketService.typedTopic<HealthSnapshot>(`${HEALTH_TOPIC}/${this.system}`)
        .subscribe(
          snapshot => {

            const format = 'HH:mm:ss';
            const locale = 'en-US';
            const formattedDate = formatDate(snapshot.timestamp, format, locale);

            this.lineChartData.labels?.splice(this.lineChartData.datasets[INDEX_CPU].data.length, 1, formattedDate)
            this.lineChartData.datasets[INDEX_CPU].data.push(Math.round(snapshot.cpu * 100));
            this.lineChartData.datasets[INDEX_RAM].data.push(Math.round(snapshot.memory * 100));
            this.lineChartData.datasets[INDEX_DISK].data.push(Math.round(snapshot.disk * 100));
            this.baseChart?.update();
            HealthChartComponent.trimArray(this.lineChartData.labels ? this.lineChartData.labels : [])
            HealthChartComponent.trimArray(this.lineChartData.datasets[INDEX_CPU].data)
            HealthChartComponent.trimArray(this.lineChartData.datasets[INDEX_RAM].data)
            HealthChartComponent.trimArray(this.lineChartData.datasets[INDEX_DISK].data)
            this.baseChart?.update();
            this.dataCount++;
          }
        );
    }
  }

}
