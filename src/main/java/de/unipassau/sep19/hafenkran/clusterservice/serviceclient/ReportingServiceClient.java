package de.unipassau.sep19.hafenkran.clusterservice.serviceclient;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ResultDTO;
import lombok.NonNull;

import java.util.Set;
import java.util.UUID;

/**
 * A service for communicating with the ReportingService.
 */
public interface ReportingServiceClient {

    /**
     * Pushes the given results to the ReportingService.
     *
     * @param resultDTO the given results.
     */
    void sendResultsToResultsService(@NonNull ResultDTO resultDTO);

    /**
     * Pushes the delete-call for the execution to the ReportingService.
     *
     * @param executionIdList The list of the executions which results should be deleted.
     */
    void deleteResults(@NonNull Set<UUID> executionIdList);

}
