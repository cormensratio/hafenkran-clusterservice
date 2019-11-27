package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import io.kubernetes.client.ApiException;
import lombok.NonNull;

import java.util.UUID;

/**
 * Interface providing methods for interacting with a KubernetesClient.
 */
public interface KubernetesClient {

    /**
     * Creates the Pod in Kubernetes.
     *
     * @return the name of the pod.
     * @throws ApiException if the pod can't be created
     */
    String createPod(@NonNull UUID experimentId, @NonNull String executionName) throws ApiException;

    /**
     * Deletes the Pod in Kubernetes.
     *
     * @throws ApiException if the pod can't be deleted
     */
    void deletePod(@NonNull UUID experimentId, @NonNull String executionName) throws ApiException;

    /**
     * Retrieve the logs for a running Pod.
     *
     * @throws ApiException if the pod can't be deleted
     */
    String retrieveLogs(@NonNull ExecutionDetails executionDetails, int lines, Integer sinceSeconds, boolean withTimestamps) throws ApiException;

}