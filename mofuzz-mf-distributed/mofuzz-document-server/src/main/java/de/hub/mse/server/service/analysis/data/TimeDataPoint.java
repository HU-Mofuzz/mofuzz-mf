package de.hub.mse.server.service.analysis.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TimeDataPoint<T extends Number> {
    private T y;
    private long x;
}
