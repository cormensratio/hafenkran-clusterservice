package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public interface ExecutionService {

    /**
     * Persists an given {@link ExecutionDetails} and saves it in the database.
     *
     * @param executionDetails The {@link ExecutionDetails}, which are going to be persisted.
     * @return The persisted {@link ExecutionDetails}.
     */
    ExecutionDetails createExecution(@NonNull ExecutionDetails executionDetails);

    /**
     * Converts an {@link ExecutionCreateDTO} to {@link ExecutionDTO} and saves its {@link ExecutionDetails} in the database.
     *
     * @param executionCreateDTO The {@link ExecutionCreateDTO} to be converted.
     * @return The conversion result as a {@link ExecutionDTO}.
     */
    ExecutionDTO createExecution(@NonNull ExecutionCreateDTO executionCreateDTO);

    ExecutionDTO findExecutionDTOById(@NonNull UUID id);

    ExecutionDetails findExecutionById(@NonNull UUID id);

    /**
     * Converts the given {@link ExecutionDetails} to its {@link ExecutionDTO} representation.
     *
     * @param execDetails The {@link ExecutionDetails}, which are going to be converted.
     * @return The converted {@link ExecutionDTO}.
     */
    ExecutionDTO convertExecDetailsToExecDTO(ExecutionDetails execDetails);

    List<ExecutionDTO> findExecutionsDTOListOfExperimentId(@NonNull UUID experimentId);


}
