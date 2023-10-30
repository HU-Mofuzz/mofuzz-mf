import {Component, OnInit} from '@angular/core';
import {ChartConfiguration, ChartOptions} from "chart.js";
import {CHART_TEXT_COLOR} from "../../../app.component";

const LIMIT_X = 100;
const STEP_WIDTH = 0.1;

@Component({
  selector: 'app-demo-plot',
  templateUrl: './demo-plot.component.html',
  styleUrls: ['./demo-plot.component.scss']
})
export class DemoPlotComponent implements OnInit {

  lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    aspectRatio: 5,
    scales: {
      y: {
        min: 0,
        ticks: {
          callback: function (val, index) {
            // Hide every 2nd tick label
            return '';
          },
        },
        title: {
          display: true,
          text: "Document Count",
          color: CHART_TEXT_COLOR,
          font: {
            size: 18
          }
        }
      },
      x: {
        beginAtZero: true,
        grace: 20,
        ticks: {
          stepSize: 1,
          autoSkip: false,
          sampleSize: LIMIT_X / STEP_WIDTH,
          includeBounds: true,
          maxRotation: 0,
          maxTicksLimit: 20,
          callback: function (val, index) {
            // Hide every 2nd tick label
            return '';
          },
        },
        title: {
          display: true,
          text: "Modelbreite / Modelhöhe / Sheetanzahl / Zieltiefe",
          color: CHART_TEXT_COLOR,
          font: {
            size: 18
          }
        }
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
        label: 'Document Count',
        fill: false,
        pointRadius: 0,
        tension: 0.1,
        borderColor: 'blue',
        backgroundColor: 'rgba(0,0,255,0.2)',
        pointBackgroundColor: 'rgba(0,0,255,0.2)'
      }
    ]
  };

  ngOnInit(): void {
    const x: number[] = [];
    const y: number[] = [];

    for (let i = 0; i < LIMIT_X; i = i + STEP_WIDTH) {
      x.push(i);
      y.push(Math.sqrt(i))
    }

    this.lineChartData.labels?.push(...x);
    this.lineChartData.datasets[0].data.push(...y);
  }

  downloadChart(chart: HTMLCanvasElement) {
    const link = document.createElement('a');
    link.href = chart.toDataURL("image/png");

    let filename = "document_count.png"
    link.download = filename;
    link.click();
  }
}
