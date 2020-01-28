package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.Set;
import java.util.UUID;

/**
 * The Data Transfer Object (DTO) representation of an {@link ExperimentDetails}.
 */
@Data
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class PermittedUsersUpdateDTO {

    @JsonProperty("permittedUsers")
    private final Set<UUID> permittedUsers;

}