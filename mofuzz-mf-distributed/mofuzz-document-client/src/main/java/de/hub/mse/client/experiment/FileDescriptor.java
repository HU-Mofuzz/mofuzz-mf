package de.hub.mse.client.experiment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDescriptor {

    private String id;
    private String experiment;

    private int depth;

    private int documentWidth;

    private int documentHeight;

    private int sheetCount;

    private List<String> linkedFiles;

}
