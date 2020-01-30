package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricDTO;

import java.util.ArrayList;

/**
 * The MetricsServerService for retrieving all podMetrics.
 */
public interface MetricsService {
    ArrayList<MetricDTO> retrieveMetrics();
}