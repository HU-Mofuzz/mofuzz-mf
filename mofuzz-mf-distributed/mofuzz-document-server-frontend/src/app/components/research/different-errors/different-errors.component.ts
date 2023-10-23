import {Component, Input} from '@angular/core';
import {Experiment} from "../../../model/experiment";
import {BackendService} from "../../../services/backend.service";
import {DifferentErrorInfo} from "../../../model/data";

@Component({
  selector: 'app-different-errors',
  templateUrl: './different-errors.component.html',
  styleUrls: ['./different-errors.component.scss']
})
export class DifferentErrorsComponent {
  @Input()
  differentErrors: {[name: string]: DifferentErrorInfo[]} = {};

  private experiments: Experiment[] = [];

  constructor(private backendService: BackendService) {}

  ngOnInit() {
    this.backendService.experiment.getExperiments().subscribe(
      experiments => this.experiments = experiments
    );
  }

  getNameOfExperiment(id: string): string {
    const found = this.experiments.find(e => e.id === id);
    return found? found.description : id;
  }

  getKeys(): string[] {
    return Object.keys(this.differentErrors)
  }

  downloadFiles(value: string) {
    this.backendService.analysis.getFileTreeForFileDescriptor(value)
  }
}
