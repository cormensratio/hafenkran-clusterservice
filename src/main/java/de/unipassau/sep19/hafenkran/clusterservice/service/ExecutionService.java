package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import lombok.NonNull;

import java.util.UUID;

public interface ExecutionService {

    ExecutionDetails createExecutionFromExecDetails(@NonNull ExecutionDetails executionDetails);

    ExecutionDetails createExecutionFromExecDTO(@NonNull ExecutionCreateDTO executionCreateDTO);

    ExecutionDetails findExecutionById(@NonNull UUID id);

    ExecutionDetails convertExecCreateDTOtoExecDetails(ExecutionCreateDTO executionCreateDTO);

}
