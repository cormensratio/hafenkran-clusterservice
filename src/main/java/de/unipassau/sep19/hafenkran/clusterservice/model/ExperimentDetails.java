package de.unipassau.sep19.hafenkran.clusterservice.model;

import de.unipassau.sep19.hafenkran.clusterservice.dto.UserDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.*;

/**
 * {@code ExperimentDetails} save the most significant data to identify a user's uploaded experiment.
 */
@Slf4j
@Data
@Table(
        name = "experimentdetails",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"ownerId", "name"})}
)
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExperimentDetails extends Resource {

    @NonNull
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "experimentDetails")
    private List<ExecutionDetails> executionDetails;

    @NonNull
    private String checksum;

    @NonNull
    @NotBlank
    private String name;

    @NonNull
    private String fileName;

    @ElementCollection
    @CollectionTable(name = "experimentdetails_permittedusers", joinColumns = @JoinColumn(name = "experimentdetails_id"))
    @Column(name = "permittedusers")
    private Set<UUID> permittedUsers;

    private long size;

    private long totalNumberOfExecutionsStarted;

    public ExperimentDetails(@NonNull UUID ownerId, @NonNull String name,
                             @NonNull String fileName, long size) {
        super(ownerId);
        this.name = name;
        this.size = size;
        this.fileName = fileName;
        this.executionDetails = new ArrayList<>();
        this.permittedUsers = new HashSet<>();
        this.permittedUsers.add(ownerId);
        this.totalNumberOfExecutionsStarted = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validatePermissions() {
        UserDTO user = SecurityContextUtil.getCurrentUserDTO();
        if (!(user.isAdmin() || user.getId().equals(this.getOwnerId()) || permittedUsers.contains(user.getId()))) {
            log.info(String.format("User %s is not allowed to access %s with id %s", user.getId(),
                    this.getClass().getName(), this.getId()));
            throw new ResourceNotFoundException(this.getClass());
        }
    }
}
