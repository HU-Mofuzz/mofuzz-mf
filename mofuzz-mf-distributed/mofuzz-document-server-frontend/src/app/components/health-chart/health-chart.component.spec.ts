import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HealthChartComponent } from './health-chart.component';

describe('ServerHealthChartComponent', () => {
  let component: HealthChartComponent;
  let fixture: ComponentFixture<HealthChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HealthChartComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HealthChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
