package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * {@code ExecutionDetails} are directly linked to {@link ExperimentDetails} and
 * save the most significant data to identify an Execution and its' related
 * ExperimentDetail. {@link ExecutionDetails} are running instances of {@link
 * ExperimentDetails} with additional data.
 */
@Data
@Table(name = "executiondetails")
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExecutionDetails extends Resource {

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", nullable = false)
    private ExperimentDetails experimentDetails;

    @NonNull
    @NotEmpty
    private String name;

    @Basic
    private String podName;

    @Basic
    private LocalDateTime startedAt;

    @Basic
    private LocalDateTime terminatedAt;

    @NonNull
    @Enumerated(EnumType.STRING)
    private Status status;

    private long ram;

    private long cpu;

    private long bookedTime;

    public ExecutionDetails(@NonNull UUID ownerId, @NonNull ExperimentDetails experimentDetails,
                            @NonNull @NotEmpty String name, long ram,
                            long cpu, long bookedTime) {
        super(ownerId);
        this.experimentDetails = experimentDetails;
        this.name = name;
        this.status = Status.WAITING;
        this.ram = ram;
        this.cpu = cpu;
        this.bookedTime = bookedTime;
    }

    public ExecutionDetails(@NonNull ExperimentDetails experimentDetails,
                            @NonNull @NotEmpty String name, long ram,
                            long cpu, long bookedTime) {
        this(experimentDetails.getId(), experimentDetails, name, ram, cpu, bookedTime);
    }

    /**
     * Where {@code RUNNING} means that the execution is currently running.
     * {@code FINISHED} means that the execution finished successfully. {@code
     * CANCELED} means that the execution got canceled by the user. {@code
     * ABORTED} means that the execution got aborted by the admin. {@code
     * FAILED} means that the execution ended due to an error. {@code WAITING}
     * means that the execution is queued.
     */
    public enum Status {
        RUNNING, FINISHED, CANCELED, ABORTED, FAILED, WAITING
    }
}
