package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.Data;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The Data Transfer Object (DTO) representation of a list of {@link ExecutionDetails}.
 */
@Data
public class ExecutionDTOList {

    @NonNull
    @JsonProperty("experimentId")
    private final UUID experimentId;

    @NonNull
    @JsonProperty("executionDTOList")
    private final List<ExecutionDTO> executionDTOList;

    @JsonCreator
    public ExecutionDTOList(@NonNull ExperimentDetails experimentDetails) {
        this.experimentId = experimentDetails.getId();
        this.executionDTOList = fromExecutionDetailsList(experimentDetails.getExecutionDetails());
    }

    /**
     * Converts a list of {@link ExecutionDetails} into a {@link ExecutionDTOList}.
     *
     * @param executionDetailsList The list of {@link ExecutionDetails} that is going to be converted.
     * @return The converted {@link ExecutionDTOList}.
     */
    public static List<ExecutionDTO> fromExecutionDetailsList(
            @NonNull List<ExecutionDetails> executionDetailsList) {

        if (executionDetailsList.isEmpty()) {
            return Collections.emptyList();
        }

        return executionDetailsList.stream()
                .map(ExecutionDTO::fromExecutionDetails).collect(Collectors.toList());
    }

}
