import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientDataPairChartComponent } from './client-data-pair-chart.component';

describe('ClientDataPairChartComponent', () => {
  let component: ClientDataPairChartComponent;
  let fixture: ComponentFixture<ClientDataPairChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClientDataPairChartComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientDataPairChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
