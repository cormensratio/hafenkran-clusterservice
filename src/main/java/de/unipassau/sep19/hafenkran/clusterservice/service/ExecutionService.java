package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public interface ExecutionService {

    /**
     * Persists a given {@link ExecutionDetails} in the experiment database.
     *
     * @param executionDetails The {@link ExecutionDetails}, which are going to be persisted in the database.
     * @return The persisted {@link ExecutionDetails}.
     */
    ExecutionDetails createExecution(@NonNull ExecutionDetails executionDetails);

    /**
     * @param id The UUID of the requested {@link ExecutionDetails}.
     * @return The requested {@link ExecutionDetails} by its {@code id}.
     */
    ExecutionDetails findExecutionById(@NonNull UUID id);

    /**
     * @param id The UUID of the requested {@link ExecutionDTO}.
     * @return The requested {@link ExecutionDTO} by its {@code id}.
     */
    ExecutionDTO findExecutionDTOById(@NonNull UUID id);

    /**
     * @param experimentId The {@code userId} of the owner's {@link ExecutionDTOList}
     * @return The list of {@link ExecutionDTO}s of the requested {@code experimentId}.
     */
    List<ExecutionDTO> findExecutionsDTOListOfExperimentId(@NonNull UUID experimentId);
}
