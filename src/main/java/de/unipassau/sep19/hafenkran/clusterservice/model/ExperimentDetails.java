package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
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
public class ExperimentDetails {

    @Id
    private UUID id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "experimentDetails")
    private List<ExecutionDetails> executionDetailsList;

    @Valid
    @NonNull
    @NotEmpty
    private String experimentName;

    @Basic
    @NonNull
    private LocalDateTime createdAt;

    @NonNull
    private UUID userId;

    private long size;

    public ExperimentDetails(@NonNull UUID userId, @NonNull String experimentName, long size) {
        this.userId = userId;
        this.experimentName = experimentName;
        this.size = size;
        this.createdAt = LocalDateTime.now();
        this.executionDetailsList = Collections.emptyList();
        this.id = UUID.randomUUID();
    }

}


