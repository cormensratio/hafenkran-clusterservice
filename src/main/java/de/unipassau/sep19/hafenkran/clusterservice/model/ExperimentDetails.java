package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String experimentName;

    @Basic
    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private UserDetails owner;

    private long size;

    public ExperimentDetails(@NotNull String experimentName, @NotNull UserDetails owner, long size) {
        this.experimentName = experimentName;
        this.owner = owner;
        this.size = size;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    private void prePersist() {
        this.id = UUID.randomUUID();
    }
}


