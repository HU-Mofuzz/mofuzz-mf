import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";


export const API_BASE = "/api/v1";
@Injectable({
  providedIn: 'root'
})
export class BackendService {

  readonly mail: MailController;
  constructor(private httpClient: HttpClient) {
    this.mail = new MailController(httpClient);
  }
}

class MailController {

  constructor(private httpClient: HttpClient) {
  }

  sendTestMail(): Observable<void> {
    return this.httpClient.post<void>(`${API_BASE}/mail/test`, null);
  }
}
