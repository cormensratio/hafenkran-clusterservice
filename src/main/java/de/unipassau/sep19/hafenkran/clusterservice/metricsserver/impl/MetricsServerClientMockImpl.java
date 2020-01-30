package de.unipassau.sep19.hafenkran.clusterservice.metricsserver.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricDTO;
import de.unipassau.sep19.hafenkran.clusterservice.metricsserver.MetricsServerClient;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Implementation of MetricsServer Mock Client for test purposes,
 * where Kubernetes and/or the MetricsServer isn't needed.
 */
@Slf4j
@Component
public class MetricsServerClientMockImpl implements MetricsServerClient {

    String regex = "[^0-9]";

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
    public ArrayList<MetricDTO> retrieveMetrics() {
        ArrayList<MetricDTO> mockPodMetricsList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        MetricDTO metricDTO = null;
        MetricDTO metricDTOTwo = null;
        MetricDTO metricDTOThree = null;
        String jsonDataSourceString = "{\"metadata\":{\"name\":\"hafenkran-1\",\"namespace\":\"cce4a685-391c-474b-a2ca-7e314edde99c\",\"creationTimestamp\":\"2020-01-16T14:51:14Z\",\"selfLink\":\"/apis/metrics.k8s.io/v1beta1/namespaces/cce4a685-391c-474b-a2ca-7e314edde99c/pods/hafenkran-1\"},\"containers\":[{\"usage\":{\"memory\":\"16496Ki\",\"cpu\":\"0\"},\"name\":\"hafenkran-1\"}],\"window\":\"1m0s\",\"timestamp\":\"2020-01-16T14:51:00Z\"}";
        try {
            metricDTO = objectMapper.readValue(jsonDataSourceString, MetricDTO.class);
            metricDTO.setExecutionId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            metricDTO.setOwnerId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

            metricDTOTwo = objectMapper.readValue(jsonDataSourceString, MetricDTO.class);
            metricDTOTwo.setExecutionId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
            metricDTOTwo.setOwnerId(UUID.fromString("00000000-0000-0000-0000-000000000002"));

            metricDTOThree = objectMapper.readValue(jsonDataSourceString, MetricDTO.class);
            metricDTOThree.setExecutionId(UUID.fromString("00000000-0000-0000-0000-000000000003"));
            metricDTOThree.setOwnerId(UUID.fromString("00000000-0000-0000-0000-000000000003"));

            Date date = new Date();
            long time = date.getTime();
            Timestamp timestamp = new Timestamp(time);

            metricDTO.setTimestamp(timestamp);
            metricDTOTwo.setTimestamp(timestamp);
            metricDTOThree.setTimestamp(timestamp);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        mockPodMetricsList.add(metricDTO);
        mockPodMetricsList.add(metricDTOTwo);
        mockPodMetricsList.add(metricDTOThree);
        return mockPodMetricsList;
    }
}
