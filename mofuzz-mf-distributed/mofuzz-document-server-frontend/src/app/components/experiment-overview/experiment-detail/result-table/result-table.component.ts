import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {Experiment} from "../../../../model/experiment";
import {ExecutionResult} from "../../../../model/execution-result";
import {BackendService} from "../../../../services/backend.service";
import {Sort} from "@angular/material/sort";
import {PageEvent} from "@angular/material/paginator";
import {ClientDescriptor} from "../../../../model/client-descriptor";

@Component({
  selector: 'app-result-table',
  templateUrl: './result-table.component.html',
  styleUrls: ['./result-table.component.scss']
})
export class ResultTableComponent implements OnChanges {

  readonly PAGE_SIZE_OPTIONS = [10, 25, 50, 100];

  displayedColumns: string[] = []

  pageSize = this.PAGE_SIZE_OPTIONS[0]
  page = 0;

  @Input() experiment: Experiment | null = null;
  @Input() clientId: string | null = null;

  data: ExecutionResult[] = [];
  totalItemCount = 0;
  sort: string  = "timestamp";
  order: string = "asc";

  private clients: ClientDescriptor[] = [];

  constructor(private backendService: BackendService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.refresh();
    if(this.clientId) {
      this.displayedColumns = ["timestamp", "duration", "fileDescriptor", "previousFile", "exception",
        "crash", "hang", "errorCount", "actions"]
    } else {
      this.displayedColumns = this.displayedColumns = ["timestamp", "duration", "originClient", "fileDescriptor", "previousFile", "exception",
        "crash", "hang", "errorCount", "actions"]
    }
  }

  refresh(): void {
    if(this.experiment) {
      this.backendService.analysis.getResults(this.experiment.id, this.clientId,
        this.sort, this.order,
        this.page, this.pageSize).subscribe(
        page => {
          this.data = page.data;
          this.totalItemCount = page.totalElements;
        }
      )
      this.backendService.clients.getClientDescriptors().subscribe(clients => this.clients = clients)
    }
  }

  sortChanged(event: Sort) {
    this.sort = event.active;
    this.order = event.direction;
    this.page = 0;
    this.refresh();
  }

  pageChanged(event: PageEvent) {
    if (event.pageSize === this.pageSize) {
      this.page = event.pageIndex;
    } else {
      this.page = 0
    }
    this.pageSize = event.pageSize
    this.refresh()
  }

  getClientNameForId(id: string): string {
    const client = this.clients.find(client => client.id === id);
    if(client) {
      return client.name
    } else {
      return id;
    }
  }

  downloadFileTree(fileId: string) {
    this.backendService.analysis.getFileTreeForFileDescriptor(fileId);
  }
}

@Component({
  selector: 'app-bool-icon',
  template: "<mat-icon [matTooltip]=\"value ? 'Yes' : 'No'\">{{value ? 'check_circle' : 'cancel'}}</mat-icon>",
  styleUrls: ['./result-table.component.scss']
})
export class BoolIconComponent {

  @Input() value = false;
}
