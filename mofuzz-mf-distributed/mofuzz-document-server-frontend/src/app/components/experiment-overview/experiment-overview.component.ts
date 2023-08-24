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

  displayedColumns = ["state", "description", "documentCount", "documentWidth", "documentHeight", "sheetsPerDocument",
    "treeDepth", "timeout", "actions"]

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
}
