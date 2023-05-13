import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {BackendService} from "../../../services/backend.service";
import {CdkDragDrop, moveItemInArray, transferArrayItem} from "@angular/cdk/drag-drop";
import {Experiment} from "../../../model/experiment";

@Component({
  selector: 'app-experiment-assignment',
  templateUrl: './experiment-assignment.component.html',
  styleUrls: ['./experiment-assignment.component.scss']
})
export class ExperimentAssignmentComponent implements OnInit, OnChanges {

  @Input()
  editingEnabled = false;

  @Input()
  assignedExperiments: string[] = [];

  @Output()
  assignmentChanged = new EventEmitter<string[]>();

  initialized = false;

  assignableExperiments: string[] = [];
  private availableExperiments: Experiment[] = [];
  constructor(private backendService: BackendService) {
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
}
