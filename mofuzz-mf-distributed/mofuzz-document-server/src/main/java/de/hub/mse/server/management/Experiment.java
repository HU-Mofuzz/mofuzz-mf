package de.hub.mse.server.management;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
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
@Table(name = "experiment")
public class Experiment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Builder.Default
    private PreparationState prepared = PreparationState.UNPREPARED;

    private String description;

    private int documentCount;

    private int documentWidth;

    private int documentHeight;

    private int treeDepth;

    private int sheetsPerDocument;

    private int timeout;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable
    private List<String> serializedLinks;

    private static void sanitizeGreater(int check, int number) {
        if(number <= check) {
            throw new ValidationException();
        }
    }


    private static void sanitizeGreaterZero(int number) {
        sanitizeGreater(0, number);
    }
    public void sanitize() {
        sanitizeGreaterZero(documentCount);
        sanitizeGreaterZero(documentWidth);
        sanitizeGreaterZero(documentHeight);
        sanitizeGreaterZero(sheetsPerDocument);
        sanitizeGreaterZero(timeout);
        sanitizeGreater(-1, treeDepth);
    }

    public List<String> getSerializedLinks() {
        return serializedLinks;
    }

    public enum PreparationState {
        @JsonEnumDefaultValue
        UNPREPARED,
        PREPARING,
        PREPARED
    }
}
