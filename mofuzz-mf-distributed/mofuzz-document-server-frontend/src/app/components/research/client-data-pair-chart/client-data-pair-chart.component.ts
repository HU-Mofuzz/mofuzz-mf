import {Component, Input} from '@angular/core';
import {ClientDataPair, ClientTrackPair} from "../../../model/data";

@Component({
  selector: 'app-client-data-pair-chart',
  templateUrl: './client-data-pair-chart.component.html',
  styleUrls: ['./client-data-pair-chart.component.scss']
})
export class ClientDataPairChartComponent {

  @Input()
  title = "";

  @Input()
  data: ClientDataPair | null = null;

  @Input()
  yLabel = "";

}
