package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Data
@Table(name = "experimentdetails", schema = "hafenkran")
public class ExperimentDetails {

    @Id
    private UUID id;

    @NonNull
    private String experimentName;

    @NonNull
    private String experimentId;

    @Basic
    private LocalDateTime createdAt;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    private UserDetails owner;

    @NonNull
    private long size;

    public ExperimentDetails(@NonNull String experimentName, @NonNull UserDetails owner, @NonNull long size) {
        this.experimentName = experimentName;
        this.createdAt = LocalDateTime.now();
        this.owner = owner;
        this.size = size;
    }

    @PrePersist
    private void prePersist(){
        this.id = UUID.randomUUID();
    }
}
