import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExperimentOverviewComponent } from './experiment-overview.component';

describe('ExperimentOverviewComponent', () => {
  let component: ExperimentOverviewComponent;
  let fixture: ComponentFixture<ExperimentOverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExperimentOverviewComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExperimentOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
