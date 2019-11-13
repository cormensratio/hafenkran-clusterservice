package de.unipassau.sep19.hafenkran.clusterservice.model;

import de.unipassau.sep19.hafenkran.clusterservice.dto.UserDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Provides a superclass for all resources.
 */
@Entity
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@EqualsAndHashCode
public class Resource {

    @Id
    private UUID id;

    @Basic
    @NonNull
    private LocalDateTime createdAt;

    @NonNull
    private UUID ownerId;

    Resource(@NonNull UUID ownerId) {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.ownerId = ownerId;
    }

    /**
     * Validates whether the given user is allowed to access the resource.
     * Throws a {@link ResourceNotFoundException} when the user does not have sufficient permissions.
     * TODO: remove user parameter and use the current user from the security context
     *
     * @param user the user for which to validate the permissions.
     */
    public void validatePermissions(UserDTO user) {
        if (!(user.isAdmin() || user.getId().equals(ownerId))) {
            throw new ResourceNotFoundException(this.getClass());
        }
    }
}
