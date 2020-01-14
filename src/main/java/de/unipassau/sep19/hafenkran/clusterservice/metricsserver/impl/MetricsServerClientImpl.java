package de.unipassau.sep19.hafenkran.clusterservice.metricsserver.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricsDTO;
import de.unipassau.sep19.hafenkran.clusterservice.metricsserver.MetricsServerClient;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of MetricsServerClient to communicate with the MetricsServer via an api call.
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetricsServerClientImpl implements MetricsServerClient {

    @Value("${clusterProxy.path}")
    private String clusterProxyPath;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, MetricsDTO> retrieveMetrics() {
        String path = "/apis/metrics.k8s.io/v1beta1/pods";
        String jsonGetResponse = get(path, String.class);
        return retrieveMetricsFromGetRequest(jsonGetResponse);
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
    private Map<String, MetricsDTO> retrieveMetricsFromGetRequest(String jsonGetResponse) {
        Map<String, MetricsDTO> mapMetrics = new HashMap<>();
        try {
            JSONObject jsonGetResponseObject = new JSONObject(jsonGetResponse);
            JSONArray jsonMetricsItemsArray = jsonGetResponseObject.getJSONArray("items");
            if (jsonMetricsItemsArray != null) {
                for (int i = 0; i < jsonMetricsItemsArray.length(); i++) {
                    JSONObject jsonItemMetadata = jsonMetricsItemsArray.getJSONObject(i).getJSONObject("metadata");
                    String podName = jsonItemMetadata.get("name").toString();
                    String namespace = jsonItemMetadata.get("namespace").toString();
                    if (!namespace.equals("kube-system") && !namespace.equals("kubernetes-dashboard")) {
                        UUID experimentId = UUID.fromString(namespace);
                        JSONObject jsonItemContainerUsage = jsonMetricsItemsArray.getJSONObject(i).getJSONArray("containers").getJSONObject(0).getJSONObject("usage");
                        String cpu = jsonItemContainerUsage.get("cpu").toString();
                        String memory = jsonItemContainerUsage.get("memory").toString();
                        MetricsDTO metricsDTO = new MetricsDTO(null, experimentId, cpu, memory);
                        mapMetrics.put(podName, metricsDTO);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mapMetrics;
    }
}
