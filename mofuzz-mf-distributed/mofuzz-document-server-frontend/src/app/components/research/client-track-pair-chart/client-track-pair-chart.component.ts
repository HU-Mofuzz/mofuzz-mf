import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ClientTrackPair} from "../../../model/data";

@Component({
  selector: 'app-client-track-pair-chart',
  templateUrl: './client-track-pair-chart.component.html',
  styleUrls: ['./client-track-pair-chart.component.scss']
})
export class ClientTrackPairChartComponent {

  @Input()
  title = "";

  @Input()
  data: ClientTrackPair | null = null;

}
