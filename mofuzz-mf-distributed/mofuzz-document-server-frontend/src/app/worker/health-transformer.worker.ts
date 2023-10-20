/// <reference lib="webworker" />

import {transformHealthData} from "../utils/data-transform";

addEventListener('message', ({ data }) => {
  const response = transformHealthData(data);
  postMessage(response);
});
