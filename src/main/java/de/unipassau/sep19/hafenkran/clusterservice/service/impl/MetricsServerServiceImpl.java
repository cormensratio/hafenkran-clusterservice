package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricDTO;
import de.unipassau.sep19.hafenkran.clusterservice.service.MetricsServerService;
import de.unipassau.sep19.hafenkran.clusterservice.metricsserver.MetricsServerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Provides {@link MetricDTO} specific service.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetricsServerServiceImpl implements MetricsServerService {

    private final MetricsServerClient metricsServerClient;

    @Override
    public ArrayList<MetricDTO> retrieveMetrics() {
        return metricsServerClient.retrieveMetrics();
    }
}