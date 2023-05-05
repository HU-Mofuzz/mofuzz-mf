import { Injectable } from '@angular/core';
import {Stomp, Client, IMessage} from "@stomp/stompjs";
import * as SockJS from 'sockjs-client';
import {CompatClient} from "@stomp/stompjs/src/compatibility/compat-client";
import {Observable, Subscription} from "rxjs";
import {messageCallbackType} from "@stomp/stompjs/src/types";

@Injectable({
  providedIn: 'root'
})
export class SocketService {

  public readonly stompClient: Client;

  private onConnectSubscriptions: {topic: string, callback: messageCallbackType, subscription: StompSubscription}[] = [];
  constructor() {
    this.stompClient = Stomp.over(new SockJS(`/socket`));
    const that = this;
    this.stompClient.onConnect = () => {
      for(const sub of that.onConnectSubscriptions) {
        const id = that.stompClient.subscribe(sub.topic, sub.callback).id;
        sub.subscription.id = id;
      }
    }
    this.stompClient.activate();
  }

  topic(topic: string): Observable<IMessage> {
    return new Observable<IMessage>(subscriber => {
      const subscription = this.subscribeTopic(topic,
        (message) => subscriber.next(message));
      return () => {
        subscription.unsubscribe();
      }
    })
  }

  private subscribeTopic(topic: string, callback: messageCallbackType): Subscription {
    if(this.stompClient.connected) {
      const id = this.stompClient.subscribe(topic, callback).id
      return new StompSubscription(id, this.stompClient);
    } else {
      const subscription = new StompSubscription('', this.stompClient);
      this.onConnectSubscriptions.push({topic, callback, subscription});
      return subscription;
    }
  }
}

class StompSubscription extends Subscription {

  constructor(public id: string, stompClient: Client) {
    super(() => stompClient.unsubscribe(this.id));
  }
}
