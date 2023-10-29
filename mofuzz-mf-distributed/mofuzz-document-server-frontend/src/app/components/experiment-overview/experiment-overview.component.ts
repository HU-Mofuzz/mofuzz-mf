import {ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {Experiment} from "../../model/experiment";
import {MatDialog} from "@angular/material/dialog";
import {BackendService} from "../../services/backend.service";
import {ToastService} from "../../services/toast.service";
import {AddExperimentDialogComponent} from "./add-experiment-dialog/add-experiment-dialog.component";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {MatTable} from "@angular/material/table";
import {CollectionViewer, DataSource} from "@angular/cdk/collections";
import {Observable, of} from "rxjs";

@Component({
  selector: 'app-experiment-overview',
  templateUrl: './experiment-overview.component.html',
  styleUrls: ['./experiment-overview.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0', visibility: 'hidden' })),
      state('expanded', style({ height: '*', visibility: 'visible' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class ExperimentOverviewComponent implements OnInit {

  dataSource = new ExpandTableDataSource()

  displayedColumns = ["state", "description", "documentCount", "documentWidth", "documentHeight", "sheetsPerDocument",
    "treeDepth", "timeout", "actions"]
  isExpansionDetailRow = (i: number, row: Object) => row.hasOwnProperty('detailRow');
  expandedElement: any;

  experimentResetDisabled = true;

  constructor(private dialog: MatDialog,
              private backendService: BackendService,
              private toastService: ToastService) {
  }

  ngOnInit() {
    this.refresh();
  }

  private refresh() {
    this.dataSource = new ExpandTableDataSource();
    this.backendService.experiment.getExperiments().subscribe(
        experiments => {
          this.dataSource = new ExpandTableDataSource(experiments)
        },
        _ => this.toastService.error("Error requesting experiment list!")
    )

    this.backendService.experiment.isResetEnabled().subscribe(
      enabled => this.experimentResetDisabled = !enabled
    )
  }

  showAddDialog() {
    this.dialog.open<AddExperimentDialogComponent, void, boolean>(AddExperimentDialogComponent, {

    }).afterClosed()
        .subscribe(saved => {
          if(saved) {
            this.toastService.info("Saved experiment successfully");
            this.refresh();
          }
        })
  }

  resetExperiment(experiment: Experiment) {
    if(confirm("All generated files and results will be deleted, are you sure?")) {
      this.backendService.experiment.resetExperiment(experiment.id).subscribe(
          _ => this.toastService.info("Experiment reset successfully!"),
          _ => this.toastService.error("Error while reseting experiment!")
      );
    }
  }

  getIconForState(prepared: string): string {
    switch (prepared) {
      case "UNPREPARED":
        return "not_started";
      case "PREPARING":
        return "pending";
      case "PREPARED":
        return "check_circle";
      default:
        return "help";
    }
  }

  getTooltipForState(prepared: string): string {
    switch (prepared) {
      case "UNPREPARED":
        return "Unprepared";
      case "PREPARING":
        return "Preparing";
      case "PREPARED":
        return "Prepared";
      default:
        return "help";
    }
  }

  toggleRow(row: any) {
    if(this.expandedElement == row) {
      this.expandedElement = null;
    } else {
      this.expandedElement = row;
    }
  }
}

class ExpandTableDataSource extends DataSource<any> {

  constructor(private experiments: Experiment[] = []) {
    super();
  }
  connect(collectionViewer: CollectionViewer): Observable<any[]> {
    const rows: any[] = [];
    this.experiments.forEach(element => rows.push(element, { detailRow: true, element }));
    return of(rows);
  }

  disconnect(collectionViewer: CollectionViewer): void {}

}
