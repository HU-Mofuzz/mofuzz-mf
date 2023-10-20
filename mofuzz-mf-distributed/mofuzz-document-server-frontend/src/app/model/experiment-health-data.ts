import {DataTrack} from "./data";

export interface ExperimentHealthData {
  cpu: DataTrack;
  memory: DataTrack;
  disk: DataTrack;
  results: DataTrack;

  totalPages: number;
  totalElements: number;
}

