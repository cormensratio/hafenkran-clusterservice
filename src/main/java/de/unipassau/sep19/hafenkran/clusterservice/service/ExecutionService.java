package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import lombok.NonNull;

import java.util.UUID;

public interface ExecutionService {

    public ExecutionDetails createExecution(@NonNull ExecutionDetails executionDetails);

    public ExecutionDetails findExecutionById(@NonNull UUID id);
}
