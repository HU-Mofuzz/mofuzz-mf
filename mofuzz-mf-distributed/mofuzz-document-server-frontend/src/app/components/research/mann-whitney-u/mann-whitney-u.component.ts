import {Component, Input} from '@angular/core';
import {MannWhitneyUTestStatistic} from "../../../model/data";

const PERCENTILE = 0.05;

@Component({
  selector: 'app-mann-whitney-u',
  templateUrl: './mann-whitney-u.component.html',
  styleUrls: ['./mann-whitney-u.component.scss']
})
export class MannWhitneyUComponent {

  @Input()
  statistic: MannWhitneyUTestStatistic | null = null;

  isSuccess(value: number): boolean {
    return value < PERCENTILE;
  }

  getTooltip(result: number) {
    var percentileText = `${(1 - PERCENTILE) * 100}%`
    if(this.isSuccess(result)) {
      return `Result passes ${percentileText} percentile`
    } else {
      return `Result fails to pass ${percentileText} percentile`
    }
  }
}
