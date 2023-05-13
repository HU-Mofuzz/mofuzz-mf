import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {ClientDescriptor} from "../../../model/client-descriptor";
import {BackendService} from "../../../services/backend.service";
import {FormControl, Validators} from "@angular/forms";
import {ToastService} from "../../../services/toast.service";

@Component({
  selector: 'app-client-detail',
  templateUrl: './client-detail.component.html',
  styleUrls: ['./client-detail.component.scss']
})
export class ClientDetailComponent implements OnChanges {

  @Input()
  client: ClientDescriptor|undefined;

  @Output()
  editsSaved = new EventEmitter<void>();

  editing = false;

  name = new FormControl('', Validators.required)

  editingCopy: ClientDescriptor|undefined;

  constructor(private backendService: BackendService,
              private toastService: ToastService) {
  }

  ngOnChanges(changes: SimpleChanges) {
    this.editingCopy = this.client;
  }

  startEditing() {
    this.editingCopy = <ClientDescriptor>{...this.client};
    this.editing = true;
  }

  cancelEditing() {
    this.editing = false;
    this.editingCopy = this.client;
  }

  save() {
    this.backendService.clients.changeClientDescriptor(this.editingCopy!).subscribe(
        _ => {
          this.editing = false;
          this.toastService.info("Saved client successfully!");
          this.editsSaved.emit();
        },
        _ => this.toastService.error("Error saving client!")
    )
  }
}
