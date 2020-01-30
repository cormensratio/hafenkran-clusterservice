package de.unipassau.sep19.hafenkran.clusterservice.metricsserver.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.sep19.hafenkran.clusterservice.config.SpringContext;
import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricDTO;
import de.unipassau.sep19.hafenkran.clusterservice.metricsserver.MetricsServerClient;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import de.unipassau.sep19.hafenkran.clusterservice.serviceclient.ServiceClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.UUID;


/**
 * Implementation of MetricsServerClient to communicate with the MetricsServer via an api call.
 */
@Slf4j
@Component
public class MetricsServerClientImpl implements MetricsServerClient {

    @Value("${kubernetes.metrics.path}")
    private String kubernetesMetricsPath;

    private ExecutionService executionService;

    private ServiceClient serviceClient;

    private List<String> excludedNamespaceList;

    public MetricsServerClientImpl() {
        this.serviceClient = SpringContext.getBean(ServiceClient.class);
        this.executionService = SpringContext.getBean(ExecutionService.class);
        buildNamespaceExclusionList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<MetricDTO> retrieveMetrics() {
        String jsonGetResponse = serviceClient.get(kubernetesMetricsPath, String.class, null);
        return retrieveMetricsFromGetRequest(jsonGetResponse);
    }

    private void buildNamespaceExclusionList() {
        excludedNamespaceList = Arrays.asList("kube-node-lease", "kube-public", "kube-system", "kubernetes-dashboard");
    }

    /**
     * Retrieves metrics from each item and adds the to the map if they arent from a cluster intern pod.
     *
     * @param jsonGetResponse The response of the metrics api call represented as string.
     * @return Returns a map with the wanted metrics of pods which arent internal.
     */
    private ArrayList<MetricDTO> retrieveMetricsFromGetRequest(@NonNull String jsonGetResponse) {
        ArrayList<MetricDTO> allPodMetrics = new ArrayList<>();
        try {
            JSONObject jsonGetResponseObject = new JSONObject(jsonGetResponse);
            JSONArray jsonMetricsItemsArray = jsonGetResponseObject.getJSONArray("items");
            if (jsonMetricsItemsArray != null) {
                for (int i = 0; i < jsonMetricsItemsArray.length(); i++) {
                    String jsonDataSourceString = jsonMetricsItemsArray.getJSONObject(i).toString();
                    MetricDTO metricDTO = buildPodMetricsDTOFromJsonString(jsonDataSourceString);
                    if (metricDTO != null && !excludedNamespaceList.contains(metricDTO.getMetadata().getNamespace())) {
                        UUID experimentId = UUID.fromString(metricDTO.getMetadata().getNamespace());
                        ExecutionDetails executionDetails = executionService.getExecutionOfPod(
                                metricDTO.getMetadata().getName(), experimentId);
                        metricDTO.setExecutionId(executionDetails.getId());
                        if (executionService.retrieveExecutionDTOById(metricDTO.getExecutionId()) != null) {
                            UUID ownerId = executionDetails.getOwnerId();
                            metricDTO.setOwnerId(ownerId);
                            allPodMetrics.add(metricDTO);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return allPodMetrics;
    }

    private MetricDTO buildPodMetricsDTOFromJsonString(@NonNull String jsonDataSourceString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            MetricDTO metricDTO = objectMapper.readValue(jsonDataSourceString, MetricDTO.class);
            return metricDTO;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
