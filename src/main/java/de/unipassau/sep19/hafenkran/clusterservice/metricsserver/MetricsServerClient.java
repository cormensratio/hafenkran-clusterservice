package de.unipassau.sep19.hafenkran.clusterservice.metricsserver;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.NodeMetricsDTO;

import java.util.ArrayList;

/**
 * Interface providing method for interacting with a MetricsServerClient.
 */
public interface MetricsServerClient {

    /**
     * Retrieves the pod metrics of all pods except internal cluster pods.
     *
     * @return Arraylist with all MetricDTOs of the Pods except from internal cluster pods.
     */
    ArrayList<MetricDTO> retrieveMetrics();

    /**
     * Retrieves node metrics of all nodes of the cluster.
     *
     * @return Arraylist with all NodeMetricDTOs of the cluster.
     */
    ArrayList<NodeMetricsDTO> retrieveNodeMetrics();
}
