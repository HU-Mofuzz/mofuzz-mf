import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientTracksChartComponent } from './client-tracks-chart.component';

describe('ClientTracksChartComponent', () => {
  let component: ClientTracksChartComponent;
  let fixture: ComponentFixture<ClientTracksChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClientTracksChartComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientTracksChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
