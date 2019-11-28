package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient;

import io.kubernetes.client.ApiException;

/**
 * Interface providing methods for interacting with a KubernetesClient.
 */
public interface KubernetesClient {

    /**
     * Creates Kubernetes Pod.
     *
     * @param userName  name of the user who creates the pod
     * @param experimentName name of the experiment which should be deployed
     * @param executionName name of the execution which should be deployed as a pod in kubernetes
     * @return the name of the pod in kubernetes
     * @throws ApiException if the communication with the api results in an error
     */
    String createPod(String userName, String experimentName, String executionName) throws ApiException;

    /**
     * Deletes Kubernetes Pod.
     *
     * @param userName name of the user who is owner of the pod
     * @param experimentName  name of the experiment where the execution is stored
     * @param podName name of the pod which should be deleted from kubernetes
     * @throws ApiException if the communication with the api results in an error
     */
    void deletePod(String userName, String experimentName, String podName) throws ApiException;

}