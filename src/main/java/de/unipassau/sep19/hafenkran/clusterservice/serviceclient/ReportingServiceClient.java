package de.unipassau.sep19.hafenkran.clusterservice.serviceclient;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ResultDTO;
import lombok.NonNull;

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
}
