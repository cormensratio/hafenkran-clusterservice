package de.unipassau.sep19.hafenkran.clusterservice.serviceclient;

import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;

/**
 * A generic service for sending authenticated REST calls to the other services.
 */
public interface ServiceClient {

    /**
     * Sends a POST request with the given body and headers to the target path and converts it to the responseType.
     *
     * @param path         the target path.
     * @param responseType the target response type.
     * @param headers      the optional headers for the request.
     * @param <T>          the response type.
     * @return the response from the server converted to the given response type class.
     */
    <T> T post(@NonNull String path, @NonNull Object body, @NonNull Class<T> responseType, @Nullable HttpHeaders headers);

    /**
     * Sends a GET request with the given body and headers to the target path and converts it to the responseType.
     *
     * @param path         the target path.
     * @param responseType the target response type.
     * @param headers      the optional headers for the request.
     * @param <T>          the response type.
     * @return the response from the server converted to the given response type class.
     */
    <T> T get(@NonNull String path, @NonNull Class<T> responseType, @Nullable HttpHeaders headers);
}
