package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Data
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class ExecutionCreateDTO {

    @NonNull
    @JsonProperty("name")
    private final Optional<String> name;

    @NonNull
    @JsonProperty("experimentId")
    private UUID experimentId;

    @NonNull
    @JsonProperty("ram")
    private Optional<Long> ram;

    @NonNull
    @JsonProperty("cpu")
    private Optional<Long> cpu;

    /**
     * The maximum timespan the user allows his execution to run
     */
    @NonNull
    @JsonProperty("bookedTime")
    private Optional<Long> bookedTime;
}
