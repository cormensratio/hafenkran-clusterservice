package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class ExecutionDTO {

    @NonNull
    @JsonProperty("id")
    private final UUID id;

    @NonNull
    @JsonProperty("experimentId")
    private UUID experimentId;

    @NonNull
    @NotBlank
    @JsonProperty("name")
    private String name;

    @NonNull
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("startedAt")
    private LocalDateTime startedAt;

    @JsonProperty("terminatedAt")
    private LocalDateTime terminatedAt;

    @NonNull
    @JsonProperty("status")
    private ExecutionDetails.Status status;

    @JsonProperty("ram")
    private long ram;

    @JsonProperty("cpu")
    private long cpu;

    @JsonProperty("bookedTime")
    private long bookedTime;

    @JsonProperty("ownerId")
    private UUID ownerId;

    public static ExecutionDTO fromExecutionDetails(@NonNull final ExecutionDetails executionDetails) {
        return new ExecutionDTO(
                executionDetails.getId(),
                executionDetails.getExperimentDetails().getId(),
                executionDetails.getName(),
                executionDetails.getCreatedAt(),
                executionDetails.getStartedAt(),
                executionDetails.getTerminatedAt(),
                executionDetails.getStatus(),
                executionDetails.getRam(),
                executionDetails.getCpu(),
                executionDetails.getBookedTime(),
                executionDetails.getOwnerId()
        );
    }
}