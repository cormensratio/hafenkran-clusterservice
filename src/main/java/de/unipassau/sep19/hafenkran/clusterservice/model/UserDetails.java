package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Data
@Table(name = "userdetails", schema = "hafenkran")
public class UserDetails {

    @Id
    @NonNull
    private UUID id;

    @NotBlank(message = "Name is required!")
    @NonNull
    private String name;

    @NotBlank(message = "Password is required!")
    @NonNull
    private String password;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner", orphanRemoval = true)
    @NonNull
    private List<ExperimentDetails> experimentDetails;

    public UserDetails(@NotBlank(message = "Name is required!") @NonNull String name, @NotBlank(message = "Password is required!") @NonNull String password, @NonNull List<ExperimentDetails> experimentDetails) {
        this.name = name;
        this.password = password;
        this.experimentDetails = experimentDetails;
    }

    @PrePersist
    private void prePersist(){
        this.id = UUID.randomUUID();
    }
}
