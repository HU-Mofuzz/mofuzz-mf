package de.hub.mse.client.experiment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DescriptorResponse {
    private FileDescriptor descriptor;
    private Set<String> fileSet;

    private int timeout;
    private int height;
    private int width;
}
