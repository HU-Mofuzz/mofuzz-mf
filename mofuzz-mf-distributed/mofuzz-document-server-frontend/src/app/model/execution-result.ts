export interface ExecutionResult {
    id: string;
    experiment: string;
    originClient: string;
    fileDescriptor: string;
    crash: boolean;
    hang: boolean;
    errorCount: number;
    duration: number;
    timestamp: number;
}
