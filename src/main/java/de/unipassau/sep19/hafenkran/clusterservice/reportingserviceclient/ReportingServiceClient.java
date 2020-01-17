package de.unipassau.sep19.hafenkran.clusterservice.reportingserviceclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ResultDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ReportingServiceClient {

    @Value("${reportingservice.path}")
    private String basePath;

    @Value("${userservice.path}")
    private String usPath;

    @Value("${service-user.name}")
    private String serviceUserName;

    @Value("${service-user.password}")
    private String serviceUserPw;

    private String post(String path, String body) {
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = authHeaders();
        headers.add("Content-Type", "application/json");
        ResponseEntity<String> response = rt.exchange(path, HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);

        if (!HttpStatus.Series.valueOf(response.getStatusCode()).equals(HttpStatus.Series.SUCCESSFUL)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Could not retrieve data from %s. Reason: %s %s", path,
                            response.getStatusCodeValue(), response.getBody()));
        }

        return response.getBody();
    }

    private String getAuthToken() {
        String loginResponse = post(usPath + "authenticate",
                String.format("{\"name\":\"%s\", \"password\":\"%s\"}", serviceUserName, serviceUserPw));
        final String jwtToken;
        try {
            jwtToken = (String) new JSONObject(loginResponse).get("jwtToken");
            return jwtToken;
        } catch (JSONException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not retrieve JWT from login");
        }
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAuthToken());
        return headers;
    }

    public void sendResultsToResultsService(@NonNull ResultDTO resultDTO) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            post(basePath + "/results", mapper.writeValueAsString(resultDTO));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Can not convert results to json");
        }
    }
}
