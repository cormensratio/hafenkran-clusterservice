package de.unipassau.sep19.hafenkran.clusterservice.model;

import de.unipassau.sep19.hafenkran.clusterservice.dto.UserDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Provides a superclass for all resources.
 */
@Slf4j
@MappedSuperclass
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Resource {

    @Id
    @NonNull
    @Column(nullable = false)
    private UUID id;

    @Basic
    @NonNull
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @NonNull
    @Column(nullable = false)
    private UUID ownerId;

    Resource(@NonNull UUID ownerId) {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.ownerId = ownerId;
    }

    /**
     * Validates whether the user in the current {@link SecurityContext} is allowed to access the resource.
     * Throws a {@link ResourceNotFoundException} when the user does not have sufficient permissions.
     */
    public void validatePermissions() {
        UserDTO user = SecurityContextUtil.getCurrentUserDTO();
        if (!(user.isAdmin() || user.getId().equals(ownerId))) {
            log.info(String.format("User %s is not allowed to access %s with id %s", user.getId(),
                    this.getClass().getName(), this.getId()));
            throw new ResourceNotFoundException(this.getClass());
        }
    }
}
