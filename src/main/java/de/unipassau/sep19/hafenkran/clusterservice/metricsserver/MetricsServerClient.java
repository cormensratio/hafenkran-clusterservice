package de.unipassau.sep19.hafenkran.clusterservice.metricsserver;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricsDTO;

import java.util.Map;

/**
 * Interface providing method for interacting with a MetricsServerClient.
 */
public interface MetricsServerClient {

    /**
     * Retrieves the pod metrics of all pods except internal cluster pods.
     *
     * @return Map with Podname as Key and MetricsDTO as the value.
     */
    Map<String, MetricsDTO> retrieveMetrics();
}
