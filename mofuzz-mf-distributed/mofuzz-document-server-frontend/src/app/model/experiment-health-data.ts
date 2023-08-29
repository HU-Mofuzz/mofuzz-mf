export interface ExperimentHealthData {
  cpu: TimeDataTrack;
  memory: TimeDataTrack;
  disk: TimeDataTrack;
  results: TimeDataTrack;

  totalPages: number;
  totalElements: number;
}

export type TimeDataTrack = TimeDataPoint[];

export interface TimeDataPoint {
  x: number;
  y: number;
}
