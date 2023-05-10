import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddExperimentDialogComponent } from './add-experiment-dialog.component';

describe('AddExperimentDialogComponent', () => {
  let component: AddExperimentDialogComponent;
  let fixture: ComponentFixture<AddExperimentDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AddExperimentDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddExperimentDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
