import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientTrackPairChartComponent } from './client-track-pair-chart.component';

describe('ClientTrackPairChartComponent', () => {
  let component: ClientTrackPairChartComponent;
  let fixture: ComponentFixture<ClientTrackPairChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClientTrackPairChartComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientTrackPairChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
