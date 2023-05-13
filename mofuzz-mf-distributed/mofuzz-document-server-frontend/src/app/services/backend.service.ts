import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Experiment} from "../model/experiment";
import {ClientDescriptor} from "../model/client-descriptor";


export const API_BASE = "/api/v1";
@Injectable({
  providedIn: 'root'
})
export class BackendService {

  readonly mail: MailController;
  readonly experiment: ExperimentController;
  readonly clients: ClientDescriptorController;

  constructor(private httpClient: HttpClient) {
    this.mail = new MailController(httpClient);
    this.experiment = new ExperimentController(httpClient);
    this.clients = new ClientDescriptorController(httpClient);
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

class ClientDescriptorController {

  constructor(private httpClient: HttpClient) {
  }

  createClientDescriptor(descriptor: ClientDescriptor): Observable<void> {
    return this.httpClient.post<void>(`${API_BASE}/clients`, descriptor);
  }

  getClientDescriptors(): Observable<ClientDescriptor[]> {
    return this.httpClient.get<ClientDescriptor[]>(`${API_BASE}/clients`);
  }

  changeClientDescriptor(descriptor: ClientDescriptor): Observable<void> {
    return this.httpClient.post<void>(`${API_BASE}/clients/${descriptor.id}`, descriptor)
  }
}
