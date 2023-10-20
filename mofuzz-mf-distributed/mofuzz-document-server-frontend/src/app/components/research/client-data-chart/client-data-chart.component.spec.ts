import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientDataChartComponent } from './client-data-chart.component';

describe('ClientDataChartComponent', () => {
  let component: ClientDataChartComponent;
  let fixture: ComponentFixture<ClientDataChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClientDataChartComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientDataChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
