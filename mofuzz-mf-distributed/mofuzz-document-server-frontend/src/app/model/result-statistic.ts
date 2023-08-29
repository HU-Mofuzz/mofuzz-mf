export interface ResultStatistic {
  crashResults: number;
  hangResults: number;
  regularResults: number;
  uniqueExceptions: ExceptionCount[];
  longestDuration: ResultDuration;
  shortestDuration: ResultDuration;
  averageDuration: number;
}

export interface ExceptionCount {
  exception: string;
  count: number;
}

export interface ResultDuration {
  file: string;
  duration: number;
}

export interface ClientResultCount {
  client: string;
  count: number;
}
