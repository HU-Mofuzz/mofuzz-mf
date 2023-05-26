import { Component } from '@angular/core';
import {Experiment} from "../../../model/experiment";
import {MatDialogRef} from "@angular/material/dialog";
import {BackendService} from "../../../services/backend.service";
import {ToastService} from "../../../services/toast.service";
import {FormControl, Validators} from "@angular/forms";

@Component({
  selector: 'app-add-experiment-dialog',
  templateUrl: './add-experiment-dialog.component.html',
  styleUrls: ['./add-experiment-dialog.component.scss']
})
export class AddExperimentDialogComponent {
    experiment: Experiment = {
        description: "",
        documentCount: 1000,
        documentHeight: 100,
        documentWidth: 100,
        id: "",
        sheetsPerDocument: 10,
        timeout: 30000,
        treeDepth: 1
    };

    private defaultValidators = [Validators.min(1), Validators.required]
    description = new FormControl('', [Validators.required])
    documentCount = new FormControl('', this.defaultValidators)
    documentWidth = new FormControl('', this.defaultValidators)
    documentHeight = new FormControl('', this.defaultValidators)
    sheetsPerDocument = new FormControl('', this.defaultValidators)
    treeDepth = new FormControl('', this.defaultValidators)
    timeout = new FormControl('', this.defaultValidators)

    constructor(private dialogRef: MatDialogRef<AddExperimentDialogComponent>,
                private backendService: BackendService,
                private toastService: ToastService) {
    }

    cancel() {
        this.dialogRef.close(false)
    }

    save() {
        this.backendService.experiment.createExperiment(this.experiment).subscribe(
            _ => this.dialogRef.close(true),
            _ => this.toastService.error("Unable to save experiment!")
        )
    }

    valid(): boolean {
        return this.description.valid
            && this.documentCount.valid
            && this.documentWidth.valid
            && this.documentHeight.valid
            && this.sheetsPerDocument.valid
            && this.treeDepth.valid
            && this.timeout.valid
    }

  calculateDocumentCount(): string {
    const count = (Math.round(Math.sqrt(
      +this.experiment.documentHeight * +this.experiment.documentWidth
      * +this.experiment.sheetsPerDocument * +this.experiment.treeDepth
    )) * (+this.experiment.treeDepth - 1)) + +this.experiment.documentCount;
    return `This will generate ${count} files`
  }
}
