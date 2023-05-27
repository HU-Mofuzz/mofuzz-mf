import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {BackendService} from "../../../services/backend.service";
import {CdkDragDrop, moveItemInArray, transferArrayItem} from "@angular/cdk/drag-drop";
import {Experiment} from "../../../model/experiment";
import {Subscription} from "rxjs";
import {SocketService} from "../../../services/socket.service";
import {ExperimentProgress} from "../../../model/experiment-progress";
import {ToastService} from "../../../services/toast.service";
import {ExecutionResult} from "../../../model/execution-result";

const RESULT_TOPIC = "/mofuzz/result";
@Component({
  selector: 'app-experiment-assignment',
  templateUrl: './experiment-assignment.component.html',
  styleUrls: ['./experiment-assignment.component.scss']
})
export class ExperimentAssignmentComponent implements OnInit, OnChanges, OnDestroy {

  @Input()
  editingEnabled = false;

  @Input()
  assignedExperiments: string[] = [];

  @Input()
  client: string|undefined;

  @Output()
  assignmentChanged = new EventEmitter<string[]>();

  initialized = false;

  assignableExperiments: string[] = [];

  lastProgress: ExperimentProgress = {
    existingResults: 0,
    generatedDocuments: 0,
    totalDocumentCount: 0
  }
  lastResultTime = 0;

  private availableExperiments: Experiment[] = [];

  private resultSubscription = new Subscription();

  constructor(private backendService: BackendService,
              private socketService: SocketService,
              private toastService: ToastService) {
  }

  ngOnInit(): void {
    this.backendService.experiment.getExperiments().subscribe(
        experiments => {
          this.availableExperiments = experiments;
          this.updateAssignableIds();
        }
    )
  }

  ngOnChanges(changes: SimpleChanges) {
    this.updateAssignableIds();
    this.updateResultSubscription("client" in changes);
  }

  ngOnDestroy() {
    this.resultSubscription.unsubscribe();
  }

  getDescriptionForId(id: string): string {
    return this.availableExperiments.find(experiment => experiment.id === id)?.description || '';
  }

  private updateAssignableIds() {
    this.assignableExperiments = this.availableExperiments.filter(
        availableExperiment => !this.assignedExperiments.find(
            assignedExperiment => assignedExperiment === availableExperiment.id))
        .map(experiment => experiment.id);
    this.assignableExperiments.sort((a,b) => {
      return this.getDescriptionForId(a).localeCompare(this.getDescriptionForId(b))
    })
  }

  onDrop(event: CdkDragDrop<string[], any>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(
          event.previousContainer.data,
          event.container.data,
          event.previousIndex,
          event.currentIndex,
      );
    }
    this.updateAssignableIds();
    this.assignmentChanged.emit(this.assignedExperiments)
  }

  private updateResultSubscription(initial: boolean) {
    if(initial && this.client && this.client.length > 0) {
      this.resultSubscription = this.socketService.typedTopic<ExecutionResult>(`${RESULT_TOPIC}/${this.client}`)
          .subscribe({
            next: result => {
              this.updateLastProgress();
              this.lastResultTime = result.timestamp;
            }
          })
    }
    this.updateLastProgress();
  }

  private updateLastProgress() {
    if(this.client && this.assignedExperiments?.length > 0) {
      this.backendService.clients.getProgress(this.client, this.assignedExperiments[0]).subscribe({
        next: progress => this.lastProgress = progress,
        error: _ => this.toastService.error("Error updating experiment progress!")
      });
    }
  }
}
