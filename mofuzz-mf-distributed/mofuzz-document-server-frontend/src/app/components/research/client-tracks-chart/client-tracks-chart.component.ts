import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ClientTracks} from "../../../model/data";
import {ChartConfiguration, ChartOptions} from "chart.js";
import {transformDataTrack} from "../../../utils/data-transform";

const INDEX_LINUX_MIN  = 0;
const INDEX_LINUX_MED  = 1;
const INDEX_LINUX_MAX  = 2;
const INDEX_LAPTOP_MIN = 3;
const INDEX_LAPTOP_MED = 4;
const INDEX_LAPTOP_MAX = 5;
const INDEX_TOWER_MIN  = 6;
const INDEX_TOWER_MED  = 7;
const INDEX_TOWER_MAX  = 8;

const DEFAULT_BORDER_DASH = [5, 5]

@Component({
  selector: 'app-client-tracks-chart',
  templateUrl: './client-tracks-chart.component.html',
  styleUrls: ['./client-tracks-chart.component.scss']
})
export class ClientTracksChartComponent implements OnChanges {

  @Input()
  title = "";

  @Input()
  data: ClientTracks | null = null;

  @Input()
  downloadHint = "";

  chartOptions: ChartOptions<'line'> = {
    responsive: true,
    aspectRatio: 3,
    scales: {
      y: {
        min: 0,
        grace: 1,
        ticks: {
          precision: 0
        }
      },
      x: {
        beginAtZero: true,
        grace: 20,
        ticks: {
          stepSize: 1,
          autoSkip: false,
          sampleSize: 1000,
          includeBounds: true,
          maxTicksLimit: 50,
          callback: function(val, index) {
            // Hide every 2nd tick label
            return index % 20 === 0 ? val : '';
          },
        }

      },
    },
    interaction: {
      intersect: false,
      mode: 'index',
    },
    animation: false
  };

  chartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Linux-Client Min',
        fill: false,
        tension: 0.3,
        borderColor: 'rgba(144,238,144, 0.7)',
        pointBackgroundColor: 'rgb(144,238,144)',
        backgroundColor: 'rgba(144,238,144,0.1)',
        pointRadius: 0,
        borderDash: [5, 5]
      },
      {
        data: [],
        label: 'Linux-Client Median',
        fill: false,
        tension: 0.3,
        borderColor: 'green',
        pointBackgroundColor: 'green',
        backgroundColor: 'rgba(0,255,0,0.1)',
        pointRadius: 0
      },
      {
        data: [],
        label: 'Linux-Client Max',
        fill: false,
        tension: 0.3,
        borderColor: 'rgba(144,238,144, 0.7)',
        pointBackgroundColor: 'rgb(144,238,144)',
        backgroundColor: 'rgba(144,238,144,0.1)',
        pointRadius: 0,
        borderDash: [5, 5]
      },
      {
        data: [],
        label: 'Laptop-Client Min',
        fill: false,
        tension: 0.3,
        borderColor: 'rgba(240,128,128, 0.7)',
        pointBackgroundColor: 'rgb(240,128,128)',
        backgroundColor: 'rgba(240,128,128,0.1)',
        pointRadius: 0,
        borderDash: [7, 7]
      },
      {
        data: [],
        label: 'Laptop-Client Median',
        fill: false,
        tension: 0.3,
        borderColor: 'red',
        pointBackgroundColor: 'red',
        backgroundColor: 'rgba(255,0,0,0.1)',
        pointRadius: 0
      },
      {
        data: [],
        label: 'Laptop-Client Max',
        fill: false,
        tension: 0.3,
        borderColor: 'rgba(240,128,128, 0.7)',
        pointBackgroundColor: 'rgb(240,128,128)',
        backgroundColor: 'rgba(240,128,128,0.1)',
        pointRadius: 0,
        borderDash: [7, 7]
      },
      {
        data: [],
        label: 'Tower-Client Min',
        fill: false,
        tension: 0.3,
        borderColor: 'rgba(135,206,250, 0.7)',
        pointBackgroundColor: 'rgb(135,206,250)',
        backgroundColor: 'rgba(135,206,250, 0.1)',
        pointRadius: 0,
        borderDash: [9, 9]
      },
      {
        data: [],
        label: 'Tower-Client Median',
        fill: false,
        tension: 0.3,
        borderColor: 'blue',
        pointBackgroundColor: 'blue',
        backgroundColor: 'rgba(0,0,255,0.1)',
        pointRadius: 0
      },
      {
        data: [],
        label: 'Tower-Client Max',
        fill: false,
        tension: 0.3,
        borderColor: 'rgba(135,206,250, 0.7)',
        pointBackgroundColor: 'rgb(135,206,250)',
        backgroundColor: 'rgb(135,206,250, 0.1)',
        pointRadius: 0,
        borderDash: [9, 9]
      }
    ]
  };

  ngOnChanges(changes: SimpleChanges): void {
    if(this.data) {
      const linuxData = transformDataTrack(this.data.linuxClient.min);

      this.chartData.labels = linuxData.x;

      this.chartData.datasets[INDEX_LINUX_MIN].data =linuxData.y;
      this.chartData.datasets[INDEX_LINUX_MED].data =  transformDataTrack(this.data.linuxClient.median).y;
      this.chartData.datasets[INDEX_LINUX_MAX].data =  transformDataTrack(this.data.linuxClient.max).y;
      this.chartData.datasets[INDEX_LAPTOP_MIN].data = transformDataTrack(this.data.laptopClient.min).y;
      this.chartData.datasets[INDEX_LAPTOP_MED].data = transformDataTrack(this.data.laptopClient.median).y;
      this.chartData.datasets[INDEX_LAPTOP_MAX].data = transformDataTrack(this.data.laptopClient.max).y;
      this.chartData.datasets[INDEX_TOWER_MIN].data =  transformDataTrack(this.data.towerClient.min).y;
      this.chartData.datasets[INDEX_TOWER_MED].data =  transformDataTrack(this.data.towerClient.median).y;
      this.chartData.datasets[INDEX_TOWER_MAX].data =  transformDataTrack(this.data.towerClient.max).y;
    }
  }

  downloadChart(chart: HTMLCanvasElement, hint: string) {
    const link = document.createElement('a');
    link.href = chart.toDataURL("image/png");

    let filename = hint.toLowerCase()
      .replaceAll(" ", "_")
      .replaceAll(".", "") + '_' +
      this.title.toLowerCase()
        .replaceAll(" ", "_")
        .replaceAll(".", "") + ".png"
    link.download = filename;
    link.click();
  }

}
