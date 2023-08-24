export interface Experiment {
    id: string;
    description: string;
    documentCount: number;
    documentWidth: number;
    documentHeight: number;
    treeDepth: number;
    sheetsPerDocument: number;
    timeout: number;
    prepared: string;
}
