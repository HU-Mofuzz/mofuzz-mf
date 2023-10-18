package de.hub.mse.server.service.analysis.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DataPoint<X extends Number, Y extends Number> {
    private X x;
    private Y y;
}
