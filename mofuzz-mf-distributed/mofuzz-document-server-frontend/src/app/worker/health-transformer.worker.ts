/// <reference lib="webworker" />

import {transformHealthData} from "../utils/health-transform-util";

addEventListener('message', ({ data }) => {
  const response = transformHealthData(data);
  postMessage(response);
});
