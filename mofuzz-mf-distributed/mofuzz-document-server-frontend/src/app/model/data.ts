export type DataTrack = DataPoint[];

export interface DataPoint {
  x: number;
  y: number;
}

export interface ClientData {
  linuxClient: DataTrack;
  laptopClient: DataTrack;
  towerClient: DataTrack;
}

export interface StatisticalTracks {
  min: DataTrack;
  median: DataTrack;
  max: DataTrack;
}

export interface ClientTracks {
  linuxClient: StatisticalTracks;
  laptopClient: StatisticalTracks;
  towerClient: StatisticalTracks;
}

export interface ClientTrackPair {
  baseline: ClientTracks;
  experiment: ClientTracks;
}

export interface ClientDataPair {
  baseline: ClientData;
  experiment: ClientData;
}

export interface QuestionOneData {
  crashes: ClientTrackPair;
  exceptionTypes: ClientTrackPair;
  errorsInSheets: ClientTracks;
}

export interface QuestionTwoData {
  absoluteTimeouts: ClientTrackPair;
  baselineTotalErrors: ClientData;
  baselineTotalDuration: ClientData;
}

export interface QuestionThreeData {
  totalTimeouts: ClientDataPair;
  averageExecutionTime: ClientDataPair;
  differentExceptions: {[name: string]: string[]}
}

export interface ResearchQuestionData {
  questionOneData: QuestionOneData;
  questionTwoData: QuestionTwoData;
  questionThreeData: QuestionThreeData
}
