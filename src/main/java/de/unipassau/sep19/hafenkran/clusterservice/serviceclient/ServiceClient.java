package de.unipassau.sep19.hafenkran.clusterservice.serviceclient;

import lombok.NonNull;
import org.springframework.http.HttpHeaders;

import javax.annotation.Nullable;

/**
 * A generic service for sending authenticated REST calls to the other services.
 */
public interface ServiceClient {
    /**
     * Sends a POST request with the given body to the target path.
     *
     * @param path    the target path.
     * @param body    the request body.
     * @param headers optional headers sent with the request.
     * @return the response of the request.
     */
    String post(@NonNull String path, @NonNull Object body, @Nullable HttpHeaders headers);
}
