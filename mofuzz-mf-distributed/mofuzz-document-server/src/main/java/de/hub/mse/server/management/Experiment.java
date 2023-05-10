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
@Table(name = "experiment")
public class Experiment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String description;

    private int documentCount;

    private int documentWidth;

    private int documentHeight;

    private int treeDepth;

    private int sheetsPerDocument;

    private int timeout;
}
