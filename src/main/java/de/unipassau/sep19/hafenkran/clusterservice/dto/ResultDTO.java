package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class ResultDTO {

    @NonNull
    @JsonProperty("executionId")
    private final UUID executionId;

    @NonNull
    @JsonProperty("ownerId")
    private final UUID ownerId;

    @NonNull
    @JsonProperty("results")
    private String results;

}
