package de.unipassau.sep19.hafenkran.clusterservice.metricsserver;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricsDTO;

import java.util.ArrayList;

/**
 * Interface providing method for interacting with a MetricsServerClient.
 */
public interface MetricsServerClient {

    /**
     * Retrieves the pod metrics of all pods except internal cluster pods.
     *
     * @return Arraylist with all MetricsDTOs of the Pods except from internal cluster pods.
     */
    ArrayList<MetricsDTO> retrieveMetrics();
}
