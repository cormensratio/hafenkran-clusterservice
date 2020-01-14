package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NonNull;

import java.util.UUID;
import java.sql.Timestamp;

/**
 * The Data Transfer Object (DTO) representation of a retrieved podMetric from the MetricsServer.
 */
@Data
public class MetricsDTO {

    @NonNull
    @JsonProperty("executionId")
    private final UUID executionId;

    @NonNull
    @JsonProperty("experimentId")
    private final UUID experimentId;

    @NonNull
    @JsonProperty("cpu")
    private final String cpu;

    @NonNull
    @JsonProperty("memory")
    private final String memory;

    @NonNull
    @JsonProperty("timestamp")
    private final Timestamp timestamp;

    public MetricsDTO(@NonNull UUID executionId, @NonNull UUID experimentId, @NonNull String cpu,
                      @NonNull String memory, @NonNull Timestamp timestamp) {
        this.executionId = executionId;
        this.experimentId = experimentId;
        this.cpu = cpu;
        this.memory = memory;
        this.timestamp = timestamp;
    }
}