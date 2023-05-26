package de.hub.mse.server.management;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "file_descriptor", indexes = {
        @Index(columnList = "experiment"),
        @Index(columnList = "experiment, depth")
})
public class FileDescriptor {

    @Id
    private String id;
    private String experiment;

    private int depth;

    private int documentWidth;

    private int documentHeight;

    private int sheetCount;

    @ElementCollection
    @CollectionTable
    private List<String> linkedFiles;

}
