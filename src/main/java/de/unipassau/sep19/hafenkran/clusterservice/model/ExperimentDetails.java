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
import javax.validation.constraints.NotBlank;
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

    @NonNull
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "experimentDetails")
    private List<ExecutionDetails> executionDetails;

    @NonNull
    private String checksum;

    @Valid
    @NonNull
    @NotBlank
    private String name;

    @NonNull
    private String fileName;

    private long size;

    private long totalNumberOfExecutionsStarted;

    public ExperimentDetails(@NonNull UUID ownerId, @NonNull String name,
                             @NonNull String fileName, long size) {
        super(ownerId);
        this.name = name;
        this.size = size;
        this.fileName = fileName;
        this.executionDetails = Collections.emptyList();
    }
}


