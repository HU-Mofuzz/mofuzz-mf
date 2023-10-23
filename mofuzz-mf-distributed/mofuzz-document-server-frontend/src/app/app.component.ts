import { Component } from '@angular/core';
import {Chart} from "chart.js";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'mofuzz-document-server-frontend';
  mode = 'research';

  constructor() {
    Chart.defaults.plugins.title.color = 'white';
    Chart.defaults.plugins.legend.labels.font = {
      size: 18
    }
    Chart.defaults.plugins.legend.labels.color = 'black';
    Chart.defaults.scales.linear.ticks.font = {
      size: 18
    }
    Chart.defaults.scales.linear.ticks.color = 'black';
    Chart.defaults.scale.ticks.font = {
      size: 18
    }
    Chart.defaults.scale.ticks.color = 'black';
    Chart.defaults.plugins.title.font = {
      size: 20,
      weight: 'bold'
    }

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
