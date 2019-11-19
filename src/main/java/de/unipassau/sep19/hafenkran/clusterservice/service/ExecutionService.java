package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTOList;
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

    /**
     * Returns the {@link ExecutionDetails} from the specified {@code id}.
     *
     * @param id The UUID of the requested {@link ExecutionDetails}.
     * @return The requested {@link ExecutionDetails} by its {@code id}.
     */
    ExecutionDetails findExecutionById(@NonNull UUID id);

    /**
     * Returns the {@link ExecutionDTO} from the specified {@code id}.
     *
     * @param id The UUID of the requested {@link ExecutionDTO}.
     * @return The requested {@link ExecutionDTO} by its {@code id}.
     */
    ExecutionDTO findExecutionDTOById(@NonNull UUID id);

    /**
     * Converts the given {@link ExecutionDetails} to its {@link ExecutionDTO} representation.
     *
     * @param execDetails The {@link ExecutionDetails}, which are going to be converted.
     * @return The converted {@link ExecutionDTO}.
     */
    ExecutionDTO convertExecDetailsToExecDTO(ExecutionDetails execDetails);


    /**
     * Returns a list of {@link ExecutionDTO}s from the specified {@code experimentId} or an empty list, if there is
     * no {@code experimentId} available.
     *
     * @param experimentId The {@code experimentId} of the owner's {@link ExecutionDTOList}
     * @return The list of {@link ExecutionDTO}s of the requested {@code experimentId}.
     */
    List<ExecutionDTO> findExecutionsDTOListOfExperimentId(@NonNull UUID experimentId);

    /**
     * Returns a list of {@link ExecutionDTO}s from the specified {@code userId} or an
     * {@link de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException}, if there is no
     * {@code userId} available.
     *
     * @param userId The {@code userId} of the owner's {@link ExecutionDTOList}
     * @return The list of {@link ExecutionDTO}s of the requested {@code userId}.
     */
    List<ExecutionDTO> findExecutionsDTOListForUserId(@NonNull UUID userId);

}
