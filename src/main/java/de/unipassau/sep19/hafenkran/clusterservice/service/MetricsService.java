package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.NodeMetricsDTO;

import java.util.ArrayList;

/**
 * The MetricsServerService for retrieving all podMetrics.
 */
public interface MetricsService {

    /**
     * Retrieves the metrics of all not cluster internal pods.
     *
     * @return Returns Arraylist of all metrics of not cluster internal pods.
     */
    ArrayList<MetricDTO> retrieveMetrics();

    /**
     * Retrieves the node metrics of all cluster nodes.
     *
     * @return Returns Arraylist of all node metrics of the cluster.
     */
    ArrayList<NodeMetricsDTO> retrieveNodeMetrics();
}