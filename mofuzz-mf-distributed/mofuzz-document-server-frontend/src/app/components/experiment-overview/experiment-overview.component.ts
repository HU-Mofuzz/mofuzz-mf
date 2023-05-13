import {Component, OnInit} from '@angular/core';
import {Experiment} from "../../model/experiment";
import {MatDialog} from "@angular/material/dialog";
import {BackendService} from "../../services/backend.service";
import {ToastService} from "../../services/toast.service";
import {AddExperimentDialogComponent} from "./add-experiment-dialog/add-experiment-dialog.component";

@Component({
  selector: 'app-experiment-overview',
  templateUrl: './experiment-overview.component.html',
  styleUrls: ['./experiment-overview.component.scss']
})
export class ExperimentOverviewComponent implements OnInit{

  experiments: Experiment[] = []

  displayedColumns = ["description", "documentCount", "documentWidth", "documentHeight", "sheetsPerDocument",
    "treeDepth", "timeout"]

  constructor(private dialog: MatDialog,
              private backendService: BackendService,
              private toastService: ToastService) {
  }

  ngOnInit() {
    this.refresh();
  }

  private refresh() {
    this.experiments = [];
    this.backendService.experiment.getExperiments().subscribe(
        experiments => this.experiments = experiments,
        _ => this.toastService.error("Error requesting experiment list!")
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
}
