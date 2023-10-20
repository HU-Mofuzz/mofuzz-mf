import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DifferentExceptionComponent } from './different-exception.component';

describe('DifferentExceptionComponent', () => {
  let component: DifferentExceptionComponent;
  let fixture: ComponentFixture<DifferentExceptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DifferentExceptionComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DifferentExceptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
