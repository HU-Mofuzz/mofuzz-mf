import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {Subscription} from "rxjs";
import {SocketService} from "../../services/socket.service";
import {HealthSnapshot} from "../../model/health-snapshot";
import {formatDate} from "@angular/common";

const HEALTH_TOPIC = "/mofuzz/health";
@Component({
  selector: 'app-health-indicator',
  templateUrl: './health-indicator.component.html',
  styleUrls: ['./health-indicator.component.scss']
})
export class HealthIndicatorComponent implements OnChanges {

  @Input()
  system: string = '';

  topicSubscription = new Subscription();

  snapshot: HealthSnapshot|undefined;

  constructor(private socketService: SocketService) {
  }

  ngOnChanges(changes: SimpleChanges) {
    if(this.system.length > 0) {
      this.topicSubscription.unsubscribe();
      this.topicSubscription = this.socketService.topic(`${HEALTH_TOPIC}/${this.system}`).subscribe(
          message => {
            this.snapshot = <HealthSnapshot>{...JSON.parse(message.body)}
          }
      )
    }
  }

  getColorClass() {
    if(!this.snapshot) {
      return 'grey-health'
    }

    var age = Date.now() - this.snapshot.timestamp
    if(age > 60000) {
      return 'yellow-health'
    } else {
      return 'green-health'
    }
  }

  getTooltip() {
    if(this.snapshot) {
      const format = 'HH:mm:ss';
      const locale = 'en-US';
      const formattedDate = formatDate(this.snapshot.timestamp, format, locale);
      const cpu = Math.round(this.snapshot.cpu * 100);
      const memory = Math.round(this.snapshot.memory * 100);
      const disk = Math.round(this.snapshot.disk * 100);
      return `CPU:${cpu}% | Memory:${memory}% |  Disk:${disk}% | Time:${formattedDate}`
    }
    return 'No health reported...';
  }

}
