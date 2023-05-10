package de.hub.mse.server.management;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "file_descriptor", indexes = {
        @Index(columnList = "experiment")
})
public class FileDescriptor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String experiment;

    private int depth;

    private int documentWidth;

    private int documentHeight;

    private int sheetCount;

}
