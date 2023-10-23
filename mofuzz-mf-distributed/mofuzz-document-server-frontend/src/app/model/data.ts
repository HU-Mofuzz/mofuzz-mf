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

  mannWhitneyUTestStatistic: MannWhitneyUTestStatistic;
}

export interface ClientDataPair {
  baseline: ClientData;
  experiment: ClientData;

  mannWhitneyUTestStatistic: MannWhitneyUTestStatistic;
}

export interface MannWhitneyUTestStatistic {
  linuxClientP: number;
  laptopClientP: number;
  towerClientP: number;
}

export interface DifferentErrorInfo {
  fileId: string;
  laptopErrors: number;
  towerErrors: number;
}
export interface QuestionOneData {
  crashes: ClientTrackPair;
  exceptionTypes: ClientTrackPair;
}

export interface QuestionTwoData {
  absoluteTimeouts: ClientTrackPair;
  baselineTotalErrors: ClientData;
  totalDuration: ClientDataPair;
  errorsInSheets: ClientTracks;

  differentErrors: {[name: string]: DifferentErrorInfo[]}
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
