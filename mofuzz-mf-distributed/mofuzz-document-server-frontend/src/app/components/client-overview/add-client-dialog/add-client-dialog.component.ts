import { Component } from '@angular/core';
import {ClientDescriptor} from "../../../model/client-descriptor";
import {FormControl, Validators} from "@angular/forms";
import {MatDialogRef} from "@angular/material/dialog";
import {BackendService} from "../../../services/backend.service";
import {ToastService} from "../../../services/toast.service";

@Component({
  selector: 'app-add-client-dialog',
  templateUrl: './add-client-dialog.component.html',
  styleUrls: ['./add-client-dialog.component.scss']
})
export class AddClientDialogComponent {

  client: ClientDescriptor = {
    id: "",
    name: "",
    description: "",
    notificationsDisabled: false,
    assignedExperiments: []
  }

  name = new FormControl('', Validators.required)

  constructor(private dialogRef: MatDialogRef<AddClientDialogComponent>,
              private backendService: BackendService,
              private toastService: ToastService) {
  }

  cancel() {
    this.dialogRef.close(false)
  }

  save() {
    this.backendService.clients.createClientDescriptor(this.client).subscribe(
        _ => this.dialogRef.close(true),
        _ => this.toastService.error("Unable to save client!")
    )
  }

  valid(): boolean {
    return this.name.valid
  }
}
