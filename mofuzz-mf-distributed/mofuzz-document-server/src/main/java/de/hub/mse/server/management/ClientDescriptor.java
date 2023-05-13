package de.hub.mse.server.management;

import de.hub.mse.server.exceptions.ValidationException;
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
@Table(name = "client_descriptor")
public class ClientDescriptor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String description;

    @ElementCollection
    @CollectionTable
    private List<String> assignedExperiments;

    public void sanitize() {
        if (name == null || name.isEmpty()) {
            throw new ValidationException();
        }
    }

}
