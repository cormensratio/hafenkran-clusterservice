package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import lombok.NonNull;

import java.util.UUID;

public interface ExecutionService {

    ExecutionDetails createExecution(@NonNull ExecutionDetails executionDetails);

    ExecutionDetails createExecutionFromExecDTO(@NonNull ExecutionCreateDTO executionCreateDTO);

    ExecutionDetails findExecutionById(@NonNull UUID id);

    ExecutionDTO convertExecDetailsToExecDTO(ExecutionDetails execDetails);

    }
