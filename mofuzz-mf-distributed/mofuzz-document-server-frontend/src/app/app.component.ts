import { Component } from '@angular/core';
import {Chart} from "chart.js";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'mofuzz-document-server-frontend';

  constructor() {
    Chart.defaults.plugins.legend.labels.color = 'white'
    Chart.defaults.plugins.title.color = 'white';
    Chart.defaults.plugins.title.font = {
      size: 20,
      weight: 'bold'
    }
  }
}
