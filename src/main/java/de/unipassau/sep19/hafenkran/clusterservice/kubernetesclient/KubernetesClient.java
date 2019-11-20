package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient;

import io.kubernetes.client.ApiException;

/**
 * Interface providing methods for interacting with a KubernetesClient.
 */
public interface KubernetesClient {

    /**
     * Creates the Pod in Kubernetes.
     *
     * @throws ApiException if the pod can't be created
     * @return the name of the pod.
     */
    String createPod() throws ApiException;

    /**
     * Deletes the Pod in Kubernetes.
     *
     * @throws ApiException if the pod can't be deleted
     */
    void deletePod() throws ApiException;

}
