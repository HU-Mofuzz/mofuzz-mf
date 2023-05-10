package de.hub.mse.server.management;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "client_descriptor")
public class ClientDescriptor {

    @Id
    private String id;

    private String name;

    private String description;
    private String assignedExperiment;

}
