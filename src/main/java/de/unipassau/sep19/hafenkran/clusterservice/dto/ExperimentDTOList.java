package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class ExperimentDTOList {

    @JsonProperty
    private final UUID userId;

    @JsonProperty
    private final List<ExperimentDTO> experimentDTOList;

    @JsonCreator
    public ExperimentDTOList(@NonNull @NotEmpty List<ExperimentDetails> experimentDetailsList) {
        this.userId = experimentDetailsList.get(0).getUserId();
        this.experimentDTOList = convertExperimentListToDTOList(experimentDetailsList);
    }

    private static ExperimentDTO convertExperimentToDTO(@NotNull @NonNull ExperimentDetails experiment) {
        return new ExperimentDTO(experiment);
    }

    public static List<ExperimentDTO> convertExperimentListToDTOList(
            @NonNull @NotEmpty List<ExperimentDetails> experimentDetailsList) {

        return experimentDetailsList.stream()
                .map(ExperimentDTOList::convertExperimentToDTO).collect(Collectors.toList());
    }
}
