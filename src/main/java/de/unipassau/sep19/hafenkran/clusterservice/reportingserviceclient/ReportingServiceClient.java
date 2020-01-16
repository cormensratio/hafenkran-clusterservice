package de.unipassau.sep19.hafenkran.clusterservice.reportingserviceclient;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.UUID;

public class ReportingServiceClient {

    @Value("${reportingservice.path}")
    private String basePath;

    private String post(String path, String body) {
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(path, HttpMethod.POST,
                new HttpEntity<>(body, authHeaders()), String.class);

        if (!HttpStatus.Series.valueOf(response.getStatusCode()).equals(HttpStatus.Series.SUCCESSFUL)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Could not retrieve data from %s. Reason: %s %s", path,
                            response.getStatusCodeValue(), response.getBody()));
        }

        return response.getBody();
    }

    private String getAuthToken() {
        String loginResponse = post("http://localhost:8081/authenticate",
                String.format("{\"name\":\"%s\", \"password\":\"%s\"}", "service", "test"));
        final String jwtToken;
        try {
            jwtToken = (String) new JSONObject(loginResponse).get("jwtToken");
            return jwtToken;
        } catch (JSONException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not retrieve JWT from login.");
        }
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAuthToken());
        return headers;
    }

    public void sendResultsToResultsService(@NonNull byte[] results, @NonNull UUID executionId) {
        post(basePath + "/results/" + executionId, Base64.getEncoder().encodeToString(results));
    }
}
