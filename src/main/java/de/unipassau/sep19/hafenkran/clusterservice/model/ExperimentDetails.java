package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.List;
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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "experimentDetails")
    private List<ExecutionDetails> executionDetailsList;

    @Valid
    @NonNull
    @NotEmpty
    private String experimentName;

    @NonNull
    private UUID userId;

    private long size;

    public ExperimentDetails(@NonNull UUID ownerId, @NonNull String experimentName, long size) {
        super(ownerId);
        this.experimentName = experimentName;
        this.size = size;
        this.executionDetailsList = Collections.emptyList();
    }

}


