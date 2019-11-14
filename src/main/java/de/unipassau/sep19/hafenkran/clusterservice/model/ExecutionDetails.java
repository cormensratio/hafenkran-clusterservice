package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.Data;
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
public class ExecutionDetails {

    @Id
    private UUID id;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experimentid", nullable = false)
    private ExperimentDetails experimentDetails;

    @NonNull
    @NotEmpty
    private String name;

    @Basic
    @NonNull
    private LocalDateTime startedAt;

    @Basic
    @NonNull
    private LocalDateTime terminatedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    private long ram;
    private long cpu;
    private long bookedTime;

    public ExecutionDetails(@NonNull ExperimentDetails experimentDetails,
                            @NonNull @NotEmpty String name, long ram,
                            long cpu, long bookedTime) {
        this.experimentDetails = experimentDetails;
        this.name = name;
        this.status = Status.WAITING;
        this.ram = ram;
        this.cpu = cpu;
        this.bookedTime = bookedTime;
    }

    public ExecutionDetails(ExperimentDetails experimentDetails){
        this.experimentDetails = experimentDetails;
        this.name = experimentDetails.getExperimentName();
        this.status = Status.WAITING;
    }

    @PrePersist
    private void prePersist() {
        this.id = UUID.randomUUID();
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
