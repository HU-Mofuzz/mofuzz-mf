import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { DebugComponent } from './components/debug/debug.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatCardModule} from "@angular/material/card";
import {MatButtonModule} from "@angular/material/button";
import {HttpClientModule} from "@angular/common/http";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatToolbarModule} from "@angular/material/toolbar";
import { ExperimentOverviewComponent } from './components/experiment-overview/experiment-overview.component';
import {MatTableModule} from "@angular/material/table";
import {MatIconModule} from "@angular/material/icon";
import {MatDialogModule} from "@angular/material/dialog";
import { AddExperimentDialogComponent } from './components/experiment-overview/add-experiment-dialog/add-experiment-dialog.component';
import {MatFormFieldModule} from "@angular/material/form-field";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import { ClientOverviewComponent } from './components/client-overview/client-overview.component';
import {MatExpansionModule} from "@angular/material/expansion";
import { ClientDetailComponent } from './components/client-overview/client-detail/client-detail.component';
import { AddClientDialogComponent } from './components/client-overview/add-client-dialog/add-client-dialog.component';
import { ExperimentAssignmentComponent } from './components/client-overview/experiment-assignment/experiment-assignment.component';
import {CdkDrag, CdkDropList} from "@angular/cdk/drag-drop";
import {MatTooltipModule} from "@angular/material/tooltip";
import { HealthIndicatorComponent } from './components/health-indicator/health-indicator.component';
import {MatBadgeModule} from "@angular/material/badge";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatRippleModule} from "@angular/material/core";
import { NgChartsModule } from 'ng2-charts';
import { HealthChartComponent } from './components/health-chart/health-chart.component';
import { ExperimentDetailComponent } from './components/experiment-overview/experiment-detail/experiment-detail.component';
import {MatProgressBarModule} from "@angular/material/progress-bar";
import { DurationPipe } from './pipes/duration.pipe';
import {
  BoolIconComponent,
  ResultTableComponent
} from './components/experiment-overview/experiment-detail/result-table/result-table.component';
import {MatSortModule} from "@angular/material/sort";
import {MatPaginatorModule} from "@angular/material/paginator";

@NgModule({
  declarations: [
    AppComponent,
    DebugComponent,
    ExperimentOverviewComponent,
    AddExperimentDialogComponent,
    ClientOverviewComponent,
    ClientDetailComponent,
    AddClientDialogComponent,
    ExperimentAssignmentComponent,
    HealthIndicatorComponent,
    HealthChartComponent,
    ExperimentDetailComponent,
    DurationPipe,
    ResultTableComponent,
    BoolIconComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    MatCardModule,
    MatButtonModule,
    HttpClientModule,
    MatSnackBarModule,
    MatToolbarModule,
    MatTableModule,
    MatIconModule,
    MatDialogModule,
    MatFormFieldModule,
    FormsModule,
    MatInputModule,
    ReactiveFormsModule,
    MatExpansionModule,
    CdkDropList,
    CdkDrag,
    MatTooltipModule,
    MatBadgeModule,
    MatCheckboxModule,
    MatRippleModule,
    NgChartsModule,
    MatProgressBarModule,
    MatSortModule,
    MatPaginatorModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
