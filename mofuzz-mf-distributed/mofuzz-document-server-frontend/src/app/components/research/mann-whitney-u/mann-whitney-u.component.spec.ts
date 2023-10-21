import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MannWhitneyUComponent } from './mann-whitney-u.component';

describe('MannWhitneyUComponent', () => {
  let component: MannWhitneyUComponent;
  let fixture: ComponentFixture<MannWhitneyUComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MannWhitneyUComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MannWhitneyUComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
