package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class ExperimentDTO {

    @JsonProperty("experimentName")
    private final String experimentName;

    @JsonProperty("createdAt")
    private final LocalDateTime createdAt;

    @JsonProperty("size")
    private final Long size;

    public ExperimentDTO(final ExperimentDetails experimentDetails) {
        this.experimentName = experimentDetails.getExperimentName();
        this.createdAt = experimentDetails.getCreatedAt();
        this.size = experimentDetails.getSize();
    }
}
