import {Component, OnInit} from '@angular/core';
import {ClientDescriptor} from "../../model/client-descriptor";
import {BackendService} from "../../services/backend.service";
import {ToastService} from "../../services/toast.service";
import {
  AddExperimentDialogComponent
} from "../experiment-overview/add-experiment-dialog/add-experiment-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {AddClientDialogComponent} from "./add-client-dialog/add-client-dialog.component";

@Component({
  selector: 'app-client-overview',
  templateUrl: './client-overview.component.html',
  styleUrls: ['./client-overview.component.scss']
})
export class ClientOverviewComponent implements OnInit {

  clients: ClientDescriptor[] = []

  constructor(private dialog: MatDialog,
              private backendService: BackendService,
              private toastService: ToastService) {
  }

  ngOnInit(): void {
    this.refresh();
  }

  refresh() {
    this.clients = []
    this.backendService.clients.getClientDescriptors().subscribe(
        clients => this.clients = clients,
        _ => this.toastService.error("Error requesting client list!")
    )
  }

  showAddDialog() {
    this.dialog.open<AddClientDialogComponent, void, boolean>(AddClientDialogComponent, {

    }).afterClosed()
        .subscribe(saved => {
          if(saved) {
            this.toastService.info("Saved client successfully");
            this.refresh();
          }
        })
  }
}
