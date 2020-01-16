package de.unipassau.sep19.hafenkran.clusterservice.reportingserviceclient;

import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.UUID;

@Service
public class ReportingServiceClient {

    @Value("${reportingservice.path}")
    private String basePath;

    private String post(String path, String body) {
        RestTemplate rt = new RestTemplate();
        String targetPath = basePath + path;
        ResponseEntity<String> response = rt.exchange(basePath + path, HttpMethod.POST, new HttpEntity<>(body, authHeaders()), String.class);

        // FIXME: better response handling
        if (!HttpStatus.Series.valueOf(response.getStatusCode()).equals(HttpStatus.Series.SUCCESSFUL)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Could not retrieve data from %s. Reason: %s %s", targetPath,
                            response.getStatusCodeValue(), response.getBody()));
        }

        return response.getBody();
    }

    private HttpHeaders authHeaders() {
        // FIXME: auth for cs in rs
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer ");
        return headers;
    }

    public String sendResultsToResultsService(@NonNull byte[] results, @NonNull UUID executionId){
        return post("/results/" + executionId, Base64.getEncoder().encodeToString(results));
    }
}
