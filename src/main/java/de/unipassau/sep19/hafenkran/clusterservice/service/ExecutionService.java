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
     * Retrieves the logs of the execution, but only if the given execution is currently running.
     *
     * @param executionId    The id of the target execution.
     * @param lines          The amount of lines to be returned.
     * @param sinceSeconds   The time in seconds defining the range from where to start the extraction of logs.
     * @param withTimestamps Show the timestamp for every line.
     * @return The string with the lines from the log.
     */
    String retrieveLogsForExecutionId(@NonNull UUID executionId, int lines, Integer sinceSeconds, boolean withTimestamps);

    /**
     * Converts an {@link ExecutionCreateDTO} to {@link ExecutionDTO}, saves its {@link ExecutionDetails} in the database
     * and starts the execution.
     *
     * @param executionCreateDTO The {@link ExecutionCreateDTO} to be converted.
     * @return The conversion result as a {@link ExecutionDTO}.
     */
    ExecutionDTO createAndStartExecution(@NonNull ExecutionCreateDTO executionCreateDTO);

    /**
     * Saves a {@link ExecutionDetails} object to the database.
     *
     * @param executionDetails The {@link ExecutionDetails} to be saved.
     * @return The saved {@link ExecutionDetails}.s
     */
    ExecutionDetails createExecution(@NonNull ExecutionDetails executionDetails);

    /**
     * Terminates the execution with the specified {@code executionId}.
     *
     * @param executionId The execution to be terminated.
     * @return An {@link ExecutionDTO} with the new changed status and the termination time.
     */
    ExecutionDTO terminateExecution(@NonNull UUID executionId);

    /**
     * Returns the {@link ExecutionDTO} from the specified {@code id}.
     *
     * @param id The UUID of the requested {@link ExecutionDTO}.
     * @return The requested {@link ExecutionDTO} by its {@code id}.
     */
    ExecutionDTO retrieveExecutionDTOById(@NonNull UUID id);

    /**
     * Returns a list of {@link ExecutionDTO}s from the specified {@code experimentId} or an empty list, if there is
     * no {@code experimentId} available.
     *
     * @param experimentId The {@code experimentId} of the owner's {@link ExecutionDTOList}
     * @return The list of {@link ExecutionDTO}s of the requested {@code experimentId}.
     */
    List<ExecutionDTO> retrieveExecutionsDTOListOfExperimentId(@NonNull UUID experimentId);

    /**
     * Returns a list of {@link ExecutionDTO}s from the specified {@code userId} or an
     * {@link de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException}, if there is no
     * {@code userId} available.
     *
     * @param userId The {@code userId} of the owner's {@link ExecutionDTOList}
     * @return The list of {@link ExecutionDTO}s of the requested {@code userId}.
     */
    List<ExecutionDTO> retrieveExecutionsDTOListForUserId(@NonNull UUID userId);

    /**
     * Deletes the execution with the given Id
     * @param executionId The {@code executionId} of the execution that should be deleted
     * @return The boolean according to wether the deletion was successful or not
     */
    boolean deleteExecution(@NonNull UUID executionId);
}
