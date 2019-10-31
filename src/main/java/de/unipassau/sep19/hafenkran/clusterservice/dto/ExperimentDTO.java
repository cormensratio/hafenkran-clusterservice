package de.unipassau.sep19.hafenkran.clusterservice.dto;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.UserDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Data
@Slf4j
public class ExperimentDTO {

    private final UUID id;
    private final String experimentName;
    private final String experimentId;
    private final LocalDateTime timestamp;
    private final UserDetails owner;
    private final Long size;

    public ExperimentDTO(final ExperimentDetails experimentDetails) {
        this.id = experimentDetails.getId();
        this.experimentName = experimentDetails.getExperimentName();
        this.experimentId = experimentDetails.getExperimentId();
        this.timestamp = experimentDetails.getCreatedAt();
        this.owner = experimentDetails.getOwner();
        this.size = experimentDetails.getSize();
    }
}
