package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricsDTO;

import java.util.Map;

public interface MetricsServerService {
    Map<String, MetricsDTO> retrieveMetrics();
}