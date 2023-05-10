import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Experiment} from "../model/experiment";


export const API_BASE = "/api/v1";
@Injectable({
  providedIn: 'root'
})
export class BackendService {

  readonly mail: MailController;
  readonly experiment: ExperimentController;

  constructor(private httpClient: HttpClient) {
    this.mail = new MailController(httpClient);
    this.experiment = new ExperimentController(httpClient);
  }
}

class MailController {

  constructor(private httpClient: HttpClient) {
  }

  sendTestMail(): Observable<void> {
    return this.httpClient.post<void>(`${API_BASE}/mail/test`, null);
  }
}

class ExperimentController {

  constructor(private httpClient: HttpClient) {
  }

  createExperiment(experiment: Experiment): Observable<void> {
    return this.httpClient.post<void>(`${API_BASE}/experiment`, experiment);
  }

  getExperiments(): Observable<Experiment[]> {
    return this.httpClient.get<Experiment[]>(`${API_BASE}/experiment`);
  }
}
