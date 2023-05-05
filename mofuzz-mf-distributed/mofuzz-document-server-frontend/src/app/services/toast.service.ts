import { Injectable } from '@angular/core';
import {MatSnackBar, MatSnackBarHorizontalPosition, MatSnackBarVerticalPosition} from "@angular/material/snack-bar";


declare type MessageType = 'info' | 'error';
@Injectable({
  providedIn: 'root'
})
export class ToastService {

  private messageQueue: {message: string, type: MessageType}[] = [];

  private currentlyShowing = false;

  constructor(private snackBar: MatSnackBar) { }

  info(message: string): void {
    this.enqueueMessage(message, 'info')
  }

  error(message: string): void {
    this.enqueueMessage(message, 'error')
  }

  private enqueueMessage(message: string, type: MessageType) {
    this.messageQueue.push({message, type})
    this.pollMessage();
  }

  private pollMessage() {
    if(!this.currentlyShowing && this.messageQueue.length > 0) {
      this.currentlyShowing = true;
      const next = this.messageQueue.pop()!;
      this.snackBar.open(`${next.type.toUpperCase()}: ${next.message}`, 'OK', {
        duration: 3000,
        horizontalPosition: 'right',
        verticalPosition: 'bottom'
      }).afterDismissed().subscribe(_ => {
          this.currentlyShowing = false;
          this.pollMessage();
        }
      )
    }
  }

}
