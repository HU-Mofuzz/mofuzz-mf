import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DifferentErrorsComponent } from './different-errors.component';

describe('DifferentErrorsComponent', () => {
  let component: DifferentErrorsComponent;
  let fixture: ComponentFixture<DifferentErrorsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DifferentErrorsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DifferentErrorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
