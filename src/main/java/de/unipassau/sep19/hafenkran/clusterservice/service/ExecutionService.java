package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import lombok.NonNull;

import java.util.UUID;

public interface ExecutionService {

    ExecutionDetails createExecution(@NonNull ExecutionDetails executionDetails);

    /**
     * Converts an {@link ExecutionCreateDTO} to {@link ExecutionDetails} and saves it in the database.
     * @param executionCreateDTO The {@link ExecutionCreateDTO} to be converted.
     * @return The conversion result as a {@link ExecutionDetails}.
     */
    ExecutionDetails createExecutionFromExecCreateDTO(@NonNull ExecutionCreateDTO executionCreateDTO);

    ExecutionDetails findExecutionById(@NonNull UUID id);

    ExecutionDTO convertExecDetailsToExecDTO(ExecutionDetails execDetails);

    }
