import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {Experiment} from "../model/experiment";
import {ClientDescriptor} from "../model/client-descriptor";
import {ExperimentProgress} from "../model/experiment-progress";
import {ClientResultCount, ResultStatistic} from "../model/result-statistic";
import {PageResponse} from "../model/page-response";
import {ExecutionResult} from "../model/execution-result";
import {ExperimentHealthData} from "../model/experiment-health-data";


export const API_BASE = "/api/v1";
@Injectable({
  providedIn: 'root'
})
export class BackendService {

  readonly mail: MailController;
  readonly experiment: ExperimentController;
  readonly clients: ClientDescriptorController;
  readonly analysis: AnalysisController;

  constructor(private httpClient: HttpClient) {
    this.mail = new MailController(httpClient);
    this.experiment = new ExperimentController(httpClient);
    this.clients = new ClientDescriptorController(httpClient);
    this.analysis = new AnalysisController(httpClient);
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

  resetExperiment(id: string): Observable<void> {
    return this.httpClient.post<void>(`${API_BASE}/experiment/reset/${id}`, null);
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

class AnalysisController {
  constructor(private httpClient: HttpClient) {
  }

  getProgress(experiment: string, client: string | null = null): Observable<ExperimentProgress> {
    let params = new HttpParams();
    if(client) {
      params = params.set("client", client)
    }
    return this.httpClient.get<ExperimentProgress>(`${API_BASE}/analysis/progress/${experiment}`, {
      params
    });
  }

  getStatistic(experiment: string, client: string | null = null): Observable<ResultStatistic> {
    let params = new HttpParams();
    if(client) {
      params = params.set("client", client)
    }
    return this.httpClient.get<ResultStatistic>(`${API_BASE}/analysis/statistic/${experiment}`, {
      params
    });
  }

  getResults(experiment: string, client: string | null = null,
             sort: string | null = null, order: string | null = null,
             page: number = 0, pageSize: number = 10): Observable<PageResponse<ExecutionResult>> {
    let params = new HttpParams().set("page", page).set("pageSize", pageSize);
    if(client) {
      params = params.set("client", client)
    }
    if(sort) {
      params = params.set("sort", sort)
    }
    if(order) {
      params = params.set("order", order)
    }
    return this.httpClient.get<PageResponse<ExecutionResult>>(`${API_BASE}/analysis/results/${experiment}`,
      {
        params
      });
  }

  getClientsWithResultsOfExperiment(experiment: string): Observable<ClientResultCount[]> {
    return this.httpClient.get<ClientResultCount[]>(`${API_BASE}/analysis/clients/${experiment}`);
  }

  getHealthData(experiment: string, client: string, page: number = 0, pageSize: number = 25): Observable<ExperimentHealthData> {
    return this.httpClient.get<ExperimentHealthData>(`${API_BASE}/analysis/health/${experiment}`,
      {
        params: new HttpParams().set("client", client).set("page", page).set("pageSize", pageSize)
      });
  }

  getFileTreeForFileDescriptor(fileId: string) {
    window.open(`/api/v1/analysis/fileTree/${fileId}`, "_blank")
  }
}
