import {ExperimentHealthData, TimeDataPoint} from "../model/experiment-health-data";

export const INDEX_CPU = 0;
export const INDEX_RAM = 1;
export const INDEX_DISK = 2;
export const INDEX_RESULTS = 3;

export function transformHealthData(healthData: ExperimentHealthData): {labels: (number | null)[], data: (number | null)[][]} {
    const labels: (number | null)[] = [];
    const data: (number | null)[][] = [];
    data[INDEX_CPU] = [];
    data[INDEX_RAM] = [];
    data[INDEX_DISK] = [];
    data[INDEX_RESULTS] = [];
    // THE BASE AXIOM OF THIS ALGORITHM IS, THAT CPU, RAM AND DISK HAVE THE SAME TIMESTAMPS
    let cpuPoint = healthData.cpu.shift();
    let resultPoint = healthData.results.shift();
    do {
      if(!cpuPoint || !resultPoint) {
        // if both are null this will skip both cases
        if(cpuPoint) {
          // health has next label
          labels.push(cpuPoint.x);
          data[INDEX_CPU].push(cpuPoint.y * 100)
          data[INDEX_RAM].push(healthData.memory.shift()!.y * 100)
          data[INDEX_DISK].push(healthData.disk.shift()!.y * 100)
          data[INDEX_RESULTS].push(null);
          cpuPoint = healthData.cpu.shift();
        } else if(resultPoint) {
          // result has next label
          labels.push(resultPoint.x);
          data[INDEX_CPU].push(null)
          data[INDEX_RAM].push(null)
          data[INDEX_DISK].push(null)
          data[INDEX_RESULTS].push(resultPoint.y);
          resultPoint = healthData.results.shift();
        }
      } else {
        if(cpuPoint.x < resultPoint.x) {
          // health has next label
          labels.push(cpuPoint.x);
          data[INDEX_CPU].push(cpuPoint.y * 100)
          data[INDEX_RAM].push(healthData.memory.shift()!.y * 100)
          data[INDEX_DISK].push(healthData.disk.shift()!.y * 100)
          data[INDEX_RESULTS].push(null);
          cpuPoint = healthData.cpu.shift();
        } else {
          // result has next label
          labels.push(resultPoint.x);
          data[INDEX_CPU].push(null)
          data[INDEX_RAM].push(null)
          data[INDEX_DISK].push(null)
          data[INDEX_RESULTS].push(resultPoint.y);
          resultPoint = healthData.results.shift();
        }
      }
    } while(healthData.cpu.length !== 0 && healthData.results.length !== 0);

    return { labels, data }
}
