package de.unipassau.sep19.hafenkran.clusterservice.serviceclient.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Suppliers;
import de.unipassau.sep19.hafenkran.clusterservice.serviceclient.ServiceClient;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * {@inheritDoc}
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ServiceClientImpl implements ServiceClient {

    @Value("${user-service-uri}")
    private String usPath;

    @Value("${service-user.name}")
    private String serviceUserName;

    @Value("${service-user.password}")
    private String serviceUserPw;

    @Value("${service-user.token-cache-time}")
    private long jwtCacheTime;

    private Supplier<String> authToken;

    /**
     * {@inheritDoc}
     */
    public <T> T get(@NonNull String path, @NonNull Class<T> responseType, @Nullable HttpHeaders headers) {
        RestTemplate rt = new RestTemplate();

        headers = headers != null ? headers : new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", authToken.get());

        ResponseEntity<T> response = rt.exchange(path, HttpMethod.GET, new HttpEntity<>("", headers), responseType);

        if (!HttpStatus.Series.valueOf(response.getStatusCode()).equals(HttpStatus.Series.SUCCESSFUL)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Could not retrieve data from %s. Reason: %s %s", path,
                            response.getStatusCodeValue(), response.getBody()));
        }

        return response.getBody();
    }


    /**
     * {@inheritDoc}
     */
    public <T> T post(@NonNull String path, @NonNull Object body, @NonNull Class<T> responseType, @Nullable HttpHeaders headers) {
        return post(path, body, responseType, headers, false);
    }

    private <T> T post(@NonNull String path, @NonNull Object body, @NonNull Class<T> responseType, @Nullable HttpHeaders headers, boolean withoutAuthHeaders) {
        RestTemplate rt = new RestTemplate();

        headers = headers != null ? headers : new HttpHeaders();
        headers.add("Content-Type", "application/json");

        if (!withoutAuthHeaders) {
            headers.add("Authorization", authToken.get());
        }

        ResponseEntity<T> response = rt.exchange(path, HttpMethod.POST,
                new HttpEntity<>(body, headers), responseType);

        if (!HttpStatus.Series.valueOf(response.getStatusCode()).equals(HttpStatus.Series.SUCCESSFUL)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Could not retrieve data from %s. Reason: %s %s", path,
                            response.getStatusCodeValue(), response.getBody()));
        }

        return response.getBody();
    }

    @PostConstruct
    private void postConstruct() {
        this.authToken = Suppliers.memoizeWithExpiration(this::retrieveAuthHeaders, jwtCacheTime, TimeUnit.SECONDS);
    }

    private String retrieveAuthHeaders() {
        String loginResponse = post(usPath + "/authenticate", new AuthenticationDTO(serviceUserName, serviceUserPw),
                String.class, null, true);

        final String jwt;
        try {
            jwt = (String) new JSONObject(loginResponse).get("jwtToken");
        } catch (JSONException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not retrieve JWT from login");
        }

        return "Bearer " + jwt;
    }

    private static class AuthenticationDTO {
        @Getter
        private String name;
        @Getter
        private String password;

        @JsonCreator
        AuthenticationDTO(@NonNull String name, @NonNull String password) {
            this.name = name;
            this.password = password;
        }
    }
}
