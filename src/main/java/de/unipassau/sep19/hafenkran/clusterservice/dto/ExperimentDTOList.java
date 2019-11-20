package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The Data Transfer Object (DTO) representation of a list of {@link ExperimentDetails}.
 */
@Data
public class ExperimentDTOList {

    @NonNull
    @JsonProperty
    private final UUID ownerId;

    @NonNull
    @JsonProperty
    private final List<ExperimentDTO> experimentDTOList;

    @JsonCreator
    public ExperimentDTOList(@NonNull List<ExperimentDetails> experimentDetailsList) {
        this.ownerId = experimentDetailsList.get(0).getOwnerId();

        if (experimentDetailsList.isEmpty()) {
            this.experimentDTOList = Collections.emptyList();
        } else {
            this.experimentDTOList = convertExperimentListToDTOList(experimentDetailsList);
        }
    }

    /**
     * Converts a list of {@link ExperimentDetails} into a {@link ExperimentDTOList}.
     *
     * @param experimentDetailsList The list of {@link ExperimentDetails} that is going to be converted.
     * @return The converted {@link ExperimentDTOList}.
     */
    public static List<ExperimentDTO> convertExperimentListToDTOList(
            @NonNull @NotEmpty List<ExperimentDetails> experimentDetailsList) {

        return experimentDetailsList.stream()
                .map(ExperimentDTO::fromExperimentDetails).collect(Collectors.toList());
    }
}
