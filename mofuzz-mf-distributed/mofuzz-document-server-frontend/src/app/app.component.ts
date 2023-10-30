import { Component } from '@angular/core';
import {Chart} from "chart.js";

export const CHART_TEXT_COLOR = "white"
export const CHART_TEXT_SIZE = 18

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'mofuzz-document-server-frontend';
  mode = 'research';

  constructor() {
    // Title
    Chart.defaults.plugins.title.color = CHART_TEXT_COLOR;
    Chart.defaults.plugins.title.font = {
      size: 20,
      weight: 'bold'
    }

    //Legend
    Chart.defaults.plugins.legend.labels.color = CHART_TEXT_COLOR;
    Chart.defaults.plugins.legend.labels.font = {
      size: CHART_TEXT_SIZE
    }

    // X Axis
    Chart.defaults.scales.linear.ticks.color = CHART_TEXT_COLOR;
    Chart.defaults.scales.linear.ticks.font = {
      size: CHART_TEXT_SIZE
    }
    Chart.defaults.scale.ticks.color = CHART_TEXT_COLOR;
    Chart.defaults.scale.ticks.font = {
      size: CHART_TEXT_SIZE
    }

    Chart.defaults.locale = "de-DE"

  }

  toggleResearch() {
    var queryParams = {}
    if(this.mode === "research") {
      this.mode = "";
    } else {
      this.mode = "research";
    }
  }
}
