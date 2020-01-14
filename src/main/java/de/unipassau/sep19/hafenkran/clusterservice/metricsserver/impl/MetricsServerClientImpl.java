package de.unipassau.sep19.hafenkran.clusterservice.metricsserver.impl;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.unipassau.sep19.hafenkran.clusterservice.config.SpringContext;
import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricsDTO;
import de.unipassau.sep19.hafenkran.clusterservice.metricsserver.MetricsServerClient;

import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.UUID;
import java.util.Date;


/**
 * Implementation of MetricsServerClient to communicate with the MetricsServer via an api call.
 */
@Slf4j
@Component
public class MetricsServerClientImpl implements MetricsServerClient {

    @Value("${clusterProxy.path}")
    private String clusterProxyPath;

    private ExecutionService executionService;

    private List<String> excludedNamespaceList;

    public MetricsServerClientImpl() {
        this.executionService = SpringContext.getBean(ExecutionService.class);
        buildNamespaceExclusionList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<MetricsDTO> retrieveMetrics() {
        String path = "/apis/metrics.k8s.io/v1beta1/pods";
        String jsonGetResponse = get(path, String.class);
        return retrieveMetricsFromGetRequest(jsonGetResponse);
    }

    private void buildNamespaceExclusionList() {
        excludedNamespaceList = Arrays.asList("kube-node-lease", "kube-public", "kube-system", "kubernetes-dashboard");
    }

    private <T> T get(String path, Class<T> responseType) {
        RestTemplate rt = new RestTemplate();
        String basePath = clusterProxyPath;
        String targetPath = basePath + path;
        ResponseEntity<T> response = rt.exchange(basePath + path, HttpMethod.GET, authHeaders(), responseType);
        if (!HttpStatus.Series.valueOf(response.getStatusCode()).equals(HttpStatus.Series.SUCCESSFUL)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Could not retrieve data from %s. Reason: %s %s", targetPath,
                            response.getStatusCodeValue(), response.getBody()));
        }

        return response.getBody();
    }

    private HttpEntity authHeaders() {
        return new HttpEntity<>(new HttpHeaders());
    }

    /**
     * Retrieves metrics from each item and adds the to the map if they arent from a cluster intern pod.
     *
     * @param jsonGetResponse The response of the metrics api call represented as string.
     * @return Returns a map with the wanted metrics of pods which arent internal.
     */
    private ArrayList<MetricsDTO> retrieveMetricsFromGetRequest(String jsonGetResponse) {
        ArrayList<MetricsDTO> allPodMetrics = new ArrayList<>();
        try {
            JSONObject jsonGetResponseObject = new JSONObject(jsonGetResponse);
            JSONArray jsonMetricsItemsArray = jsonGetResponseObject.getJSONArray("items");
            if (jsonMetricsItemsArray != null) {
                for (int i = 0; i < jsonMetricsItemsArray.length(); i++) {
                    String jsonDataSourceString = jsonMetricsItemsArray.getJSONObject(i).toString();
                    MetricsDTO metricsDTO = buildPodMetricsDTOFromJsonString(jsonDataSourceString);
                    if (metricsDTO != null) {
                        allPodMetrics.add(metricsDTO);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return allPodMetrics;
    }

    private MetricsDTO buildPodMetricsDTOFromJsonString(String jsonDataSourceString) {
        String jsonPathPodNamePath = "$['metadata']['name']";
        String jsonPathNamespacePath = "$['metadata']['namespace']";
        String jsonPathCpuUsagePath = "$['containers'][0]['usage']['cpu']";
        String jsonPathMemoryUsagePath = "$['containers'][0]['usage']['memory']";
        DocumentContext jsonContext = JsonPath.parse(jsonDataSourceString);
        String podName = jsonContext.read(jsonPathPodNamePath);
        String namespace = jsonContext.read(jsonPathNamespacePath);
        if (!excludedNamespaceList.contains(namespace)) {
            UUID experimentId = UUID.fromString(namespace);
            UUID executionId = executionService.getExecutionOfPod(podName, experimentId).getId();
            String cpu = jsonContext.read(jsonPathCpuUsagePath);
            String memory = jsonContext.read(jsonPathMemoryUsagePath);
            Date date = new Date();
            long time = date.getTime();
            Timestamp timestamp = new Timestamp(time);
            return new MetricsDTO(executionId, experimentId, cpu, memory, timestamp);
        }
        return null;
    }
}
