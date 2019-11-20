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
    @JsonProperty("userId")
    private final UUID userId;

    @NonNull
    @JsonProperty("createdAt")
    private final LocalDateTime createdAt;

    @NonNull
    @JsonProperty("size")
    private final Long size;

    public ExperimentDTO(@NonNull final ExperimentDetails experimentDetails) {
        this.id = experimentDetails.getId();
        this.name = experimentDetails.getExperimentName();
        this.userId = experimentDetails.getUserId();
        this.createdAt = experimentDetails.getCreatedAt();
        this.size = experimentDetails.getSize();
    }
}
