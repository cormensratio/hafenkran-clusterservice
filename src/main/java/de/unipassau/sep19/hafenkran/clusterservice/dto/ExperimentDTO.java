package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The Data Transfer Object (DTO) representation of an {@link ExperimentDetails}.
 */
@Data
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class ExperimentDTO {

    @NonNull
    @JsonProperty("id")
    private final UUID id;

    @NonNull
    @JsonProperty("name")
    private final String name;

    @NonNull
    @JsonProperty("ownerId")
    private final UUID ownerId;

    @NonNull
    @JsonProperty("createdAt")
    private final LocalDateTime createdAt;

    @NonNull
    @JsonProperty("size")
    private final Long size;

    public static ExperimentDTO fromExperimentDetails(@NonNull final ExperimentDetails experimentDetails) {
        return new ExperimentDTO(
                experimentDetails.getId(),
                experimentDetails.getExperimentName(),
                experimentDetails.getOwnerId(),
                experimentDetails.getCreatedAt(),
                experimentDetails.getSize()
        );
    }
}
