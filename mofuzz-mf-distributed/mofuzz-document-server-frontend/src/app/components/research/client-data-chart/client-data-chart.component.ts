import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ClientData, ClientTracks} from "../../../model/data";
import {ChartConfiguration, ChartOptions} from "chart.js";
import {transformDataTrack} from "../../../utils/data-transform";

const INDEX_LINUX  = 0;
const INDEX_LAPTOP  = 1;
const INDEX_TOWER  = 2;

@Component({
  selector: 'app-client-data-chart',
  templateUrl: './client-data-chart.component.html',
  styleUrls: ['./client-data-chart.component.scss']
})
export class ClientDataChartComponent implements OnChanges {
  @Input()
  title = "";

  @Input()
  data: ClientData | null = null;

  @Input()
  downloadHint = "";

  chartOptions: ChartOptions<'bar'> = {
    responsive: true,
    aspectRatio: 3,
    scales: {
      y: {
        min: 0
      },
      x: {
        ticks: {
          callback: function(val, index) {
            return "Experiment "+ (+val+1)
          }
        }
      }
    },
    interaction: {
      intersect: false,
      mode: 'index',
    },
    animation: false
  };

  chartData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Linux-Client',
        borderColor: 'green',
        backgroundColor: 'rgba(0,255,0,0.1)',
        borderWidth: 2,
      },
      {
        data: [],
        label: 'Laptop-Client',
        borderColor: 'red',
        backgroundColor: 'rgba(255,0,0,0.1)',
        borderWidth: 2,
      },
      {
        data: [],
        label: 'Tower-Client',
        borderColor: 'blue',
        backgroundColor: 'rgba(0,0,255,0.1)',
        borderWidth: 2,
      },
    ]
  };

  ngOnChanges(changes: SimpleChanges): void {
    if(this.data) {
      const linuxData = transformDataTrack(this.data.linuxClient);

      this.chartData.labels = linuxData.x;

      this.chartData.datasets[INDEX_LINUX].data =linuxData.y;
      this.chartData.datasets[INDEX_LAPTOP].data =  transformDataTrack(this.data.laptopClient).y;
      this.chartData.datasets[INDEX_TOWER].data =  transformDataTrack(this.data.towerClient).y;
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
