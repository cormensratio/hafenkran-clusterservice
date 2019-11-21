package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient;

import io.kubernetes.client.ApiException;

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
    String createPod(UUID experimentId, String executionName) throws ApiException;

    /**
     * Deletes the Pod in Kubernetes.
     *
     * @throws ApiException if the pod can't be deleted
     */
    void deletePod(UUID experimentId, String executionName) throws ApiException;

}
