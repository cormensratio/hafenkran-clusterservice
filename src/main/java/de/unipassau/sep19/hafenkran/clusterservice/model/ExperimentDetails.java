package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * {@code ExperimentDetails} save the most significant data to identify a user's uploaded experiment.
 */
@Data
@Table(name = "experimentdetails")
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExperimentDetails extends Resource {

    @Valid
    @NonNull
    @NotEmpty
    private String experimentName;

    private long size;

    public ExperimentDetails(@NonNull UUID ownerId, @NonNull String experimentName, long size) {
        super(ownerId);
        this.experimentName = experimentName;
        this.size = size;
    }

}


