import {Component, Input, OnInit} from '@angular/core';
import {BackendService} from "../../../services/backend.service";
import {Experiment} from "../../../model/experiment";

@Component({
  selector: 'app-different-exception',
  templateUrl: './different-exception.component.html',
  styleUrls: ['./different-exception.component.scss']
})
export class DifferentExceptionComponent implements OnInit {

  @Input()
  differentExceptions: {[name: string]: string[]} = {};

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
    return Object.keys(this.differentExceptions)
  }

  downloadFiles(value: string) {
    this.backendService.analysis.getFileTreeForFileDescriptor(value)
  }
}
