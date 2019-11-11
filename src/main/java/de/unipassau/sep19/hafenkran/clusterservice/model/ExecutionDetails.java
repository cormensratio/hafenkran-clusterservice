package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * {@code ExecutionDetails} are directly linked to {@link ExperimentDetails}
 * and save the most significant data to identify an Execution and its'
 * related ExperimentDetail. {@link ExecutionDetails} are running instances
 * of {@link ExperimentDetails} with additional data.
 */
@Data
@Table(name = "executionDetails")
@Entity
@NoArgsConstructor
public class ExecutionDetails {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "experimentId", nullable = false,
            referencedColumnName = "id")
    private ExperimentDetails experiment;

    @NonNull
    @NotEmpty
    private String executionName;

    @Basic
    @NonNull
    private LocalDateTime startedAt;

    @Basic
    @NonNull
    private LocalDateTime finishedAt;

    /**
     * Where {@code RUNNING} means that the execution is currently running.
     * {@code FINISHED} means that the execution finished successfully.
     * {@code CANCELED} means that the execution got canceled by the user.
     * {@code ABORTED} means that the execution got aborted by the admin.
     */
    private enum Status {
        RUNNING, FINISHED, CANCELED, ABORTED
    }

    @Enumerated(EnumType.ORDINAL)
    private Status status;






}
