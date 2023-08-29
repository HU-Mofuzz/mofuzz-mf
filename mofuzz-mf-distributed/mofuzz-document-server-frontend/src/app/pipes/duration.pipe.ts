import { Pipe, PipeTransform } from '@angular/core';
import {formatNumber} from "@angular/common";

const SECOND_MS = 1000;
const MINUTE_MS = 60 * SECOND_MS;

@Pipe({
  name: 'duration'
})
export class DurationPipe implements PipeTransform {

  public padIntegerLeftWithZeros(rawInteger: number, numberOfDigits: number): string {
    let paddedInteger: string = rawInteger + '';
    while (paddedInteger.length < numberOfDigits) {
      paddedInteger = '0' + paddedInteger;
    }
    return paddedInteger;
  }

  transform(value: number): unknown {
    if(value < SECOND_MS) {
      return `${value} ms`
    } else if (value < MINUTE_MS) {
      const ms = value % SECOND_MS;
      const s = (value-ms)/SECOND_MS;
      return `${s}.${this.padIntegerLeftWithZeros(ms, 3)} seconds`
    } else {
      const s = (value % MINUTE_MS) / SECOND_MS;
      const seconds = Math.round(s);
      const minutes = (value - (s * SECOND_MS)) / MINUTE_MS
      return `${minutes} minute${(minutes === 1 ? '' : 's')} ${seconds} second${seconds === 1 ? '' : 's'}`
    }
  }

}
