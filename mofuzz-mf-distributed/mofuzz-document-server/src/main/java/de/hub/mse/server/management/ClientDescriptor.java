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

    public boolean hasAssignedExperiments() {
        return assignedExperiments != null && !assignedExperiments.isEmpty();
    }

    public String getCurrentExperiment() {
        if(hasAssignedExperiments()) {
            return assignedExperiments.get(0);
        }
        return null;
    }

    public String moveToNextExperiment() {
        if(hasAssignedExperiments()) {
            this.assignedExperiments.remove(0);
        }
        if(hasAssignedExperiments()) {
            return this.assignedExperiments.get(0);
        } else {
            return null;
        }
    }

}
