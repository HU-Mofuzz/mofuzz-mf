import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {BackendService} from "../services/backend.service";
import {ToastService} from "../services/toast.service";
import {SocketService} from "../services/socket.service";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-debug',
  templateUrl: './debug.component.html',
  styleUrls: ['./debug.component.scss']
})
export class DebugComponent implements OnInit, OnDestroy {

  logs = ''

  @ViewChild('logConsole') logConsole: ElementRef|undefined;

  private logSubscription = new Subscription();

  constructor(private backendService: BackendService,
              private toastService: ToastService,
              public socketService: SocketService) {
  }

  ngOnInit() {
    this.logSubscription = this.socketService.topic('/mofuzz/logs').subscribe(
      message => {
        this.logs += message.body
        if(!!this.logConsole) {
          this.logConsole.nativeElement.scrollTop = this.logConsole.nativeElement.scrollHeight;
        }
      }
    );
  }

  ngOnDestroy() {
    this.logSubscription.unsubscribe();
  }

  sendTestMail() {
    this.backendService.mail.sendTestMail().subscribe(
      success => this.toastService.info('Test mail send successfully'),
      error => this.toastService.error('Sending test mail failed!')
    )
  }

}
