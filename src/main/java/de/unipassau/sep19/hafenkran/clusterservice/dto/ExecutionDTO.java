package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class ExecutionDTO {

    @NonNull
    @JsonProperty("id")
    private final UUID id;

    @NonNull
    @JsonProperty
    private UUID experimentId;

    @NonNull
    @JsonProperty("startedAt")
    private LocalDateTime startedAt;

    @NonNull
    @JsonProperty("terminatedAt")
    private LocalDateTime terminatedAt;

    @NonNull
    @JsonProperty("status")
    private ExecutionDetails.Status status;

    public ExecutionDTO(@NonNull final ExecutionDetails executionDetails) {
        this.id = executionDetails.getId();
        this.experimentId = executionDetails.getExperimentDetails().getId();
        this.startedAt = executionDetails.getStartedAt();
        this.terminatedAt = executionDetails.getTerminatedAt();
        this.status = executionDetails.getStatus();
    }

}
