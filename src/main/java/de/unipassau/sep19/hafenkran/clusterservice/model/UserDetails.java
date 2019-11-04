package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Table(name = "userdetails")
@Entity
@NoArgsConstructor
public class UserDetails {

    @Id
    @NotNull
    @NonNull
    private UUID id;

    @NotNull
    @NonNull
    @NotBlank(message = "Name is required!")
    private String name;

    @NotNull
    @NonNull
    @NotBlank(message = "Password is required!")
    private String password;

    public UserDetails(@NotNull @NonNull @NotBlank(message = "Name is required!") String name,
                       @NotNull @NonNull @NotBlank(message = "Password is required!") String password) {
        this.name = name;
        this.password = password;
    }

    /*
    public UserDetails(@NotBlank(message = "Name is required!") @NonNull String name,
                       @NotBlank(message = "Password is required!") @NonNull String password,
                       @NonNull List<ExperimentDetails> experimentDetailsList) {
        this.name = name;
        this.password = password;
        this.experimentDetailsList = experimentDetailsList;
    }

     */

    @PrePersist
    private void prePersist() {
        this.id = UUID.randomUUID();
    }
}
