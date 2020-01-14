package de.unipassau.sep19.hafenkran.clusterservice.metricsserver.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricsDTO;
import de.unipassau.sep19.hafenkran.clusterservice.metricsserver.MetricsServerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of MetricsServer Mock Client for test purposes,
 * where Kubernetes and/or the MetricsServer isn't needed.
 */
@Slf4j
@Component
public class MetricsServerClientMockImpl implements MetricsServerClient {

    /**
     * Constructor of MetricsServerClientMockImpl.
     * <p>
     * Prints out info that mockMetricsServerClient is used.
     */
    public MetricsServerClientMockImpl() {
        log.info("Using MetricsServerMockClient: Set mockMetricsServerClient to false in application-dev.yml" +
                " and set the path to the cluster proxy if you want to use the Metrics Server.");
    }

    @Override
    public Map<String, MetricsDTO> retrieveMetrics() {
        UUID uuid = UUID.fromString("550e8400-e29b-11d4-a716-446655440000");
        MetricsDTO mockMetricOne = new MetricsDTO(uuid, uuid, "1m", "1Ki");
        MetricsDTO mockMetricTwo = new MetricsDTO(uuid, uuid, "2m", "2Ki");
        MetricsDTO mockMetricThree = new MetricsDTO(uuid, uuid, "3m", "3Ki");

        Map<String, MetricsDTO> map = new HashMap<>();
        map.put(uuid.toString(), mockMetricOne);
        map.put(uuid.toString(), mockMetricTwo);
        map.put(uuid.toString(), mockMetricThree);

        return map;
    }
}
