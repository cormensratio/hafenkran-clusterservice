package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricsDTO {

    @JsonProperty
    private UUID executionId;

    @NonNull
    @JsonProperty("experimentId")
    private final UUID experimentId;

    @NonNull
    @JsonProperty("cpu")
    private final String cpu;

    @NonNull
    @JsonProperty("memory")
    private final String memory;

    @JsonCreator
    public MetricsDTO(UUID executionId, @NonNull UUID experimentId, @NonNull String cpu, @NonNull String memory) {
        this.executionId = executionId;
        this.experimentId = experimentId;
        this.cpu = cpu;
        this.memory = memory;
    }

    public void setExecutionId(@NonNull UUID executionId) {
        this.executionId = executionId;
    }
}