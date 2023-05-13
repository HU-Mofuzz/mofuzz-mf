import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExperimentAssignmentComponent } from './experiment-assignment.component';

describe('ExperimentAssignmentComponent', () => {
  let component: ExperimentAssignmentComponent;
  let fixture: ComponentFixture<ExperimentAssignmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExperimentAssignmentComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExperimentAssignmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
