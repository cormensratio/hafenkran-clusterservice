package de.unipassau.sep19.hafenkran.clusterservice.metricsserver.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricsDTO;
import de.unipassau.sep19.hafenkran.clusterservice.metricsserver.MetricsServerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
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
    public ArrayList<MetricsDTO> retrieveMetrics() {
        UUID executionId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID experimentId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String cpu = getRandomNumberInRange(100, 400) + "m";
        String memory = getRandomNumberInRange(1000, 4000) + "Ki";
        Date date = new Date();
        long time = date.getTime();
        Timestamp timestamp = new Timestamp(time);
        MetricsDTO mockColdFusionAlgorithmMetric = new MetricsDTO(executionId, experimentId, cpu, memory, timestamp);

        String cpuTwo = getRandomNumberInRange(100, 400) + "m";
        String memoryTwo = getRandomNumberInRange(1000, 4000) + "Ki";
        MetricsDTO mockColdFusionAlgorithmMetricTwo = new MetricsDTO(executionId, experimentId, cpuTwo, memoryTwo, timestamp);

        String cpuThree = getRandomNumberInRange(100, 400) + "m";
        String memoryThree = getRandomNumberInRange(1000, 4000) + "Ki";
        MetricsDTO mockColdFusionAlgorithmMetricThree = new MetricsDTO(executionId, experimentId, cpuThree, memoryThree, timestamp);

        ArrayList<MetricsDTO> mockPodMetricsList = new ArrayList<>();
        mockPodMetricsList.add(mockColdFusionAlgorithmMetric);
        mockPodMetricsList.add(mockColdFusionAlgorithmMetricTwo);
        mockPodMetricsList.add(mockColdFusionAlgorithmMetricThree);

        return mockPodMetricsList;
    }

    private int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
