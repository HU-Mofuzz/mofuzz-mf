import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DemoPlotComponent } from './demo-plot.component';

describe('DemoPlotComponent', () => {
  let component: DemoPlotComponent;
  let fixture: ComponentFixture<DemoPlotComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DemoPlotComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DemoPlotComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
