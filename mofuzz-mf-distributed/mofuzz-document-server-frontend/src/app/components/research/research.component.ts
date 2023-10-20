import {Component, OnInit} from '@angular/core';
import {ResearchQuestionData} from "../../model/data";
import {BackendService} from "../../services/backend.service";

@Component({
  selector: 'app-research',
  templateUrl: './research.component.html',
  styleUrls: ['./research.component.scss']
})
export class ResearchComponent implements OnInit {

  data: ResearchQuestionData | null = null;

  constructor(private backendService: BackendService) {
  }

  ngOnInit() {
    this.backendService.analysis.getResearchQuestionData().subscribe(
      data => this.data = data
    );
  }
}
