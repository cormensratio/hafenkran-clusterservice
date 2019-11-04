package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table(name = "experimentdetails")
@Entity
@NoArgsConstructor
public class ExperimentDetails {

    @Id
    private UUID id;

    @NotNull
    @NonNull
    private String experimentName;

    @Basic
    @NotNull
    @NonNull
    private LocalDateTime createdAt;

    @NotNull
    @NonNull
    private UUID userId;

    private long size;

    public ExperimentDetails(@NotNull @NonNull UUID userId, @NotNull @NonNull String experimentName, long size) {
        this.userId = userId;
        this.experimentName = experimentName;
        this.size = size;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    private void prePersist() {
        this.id = UUID.randomUUID();
    }
}


