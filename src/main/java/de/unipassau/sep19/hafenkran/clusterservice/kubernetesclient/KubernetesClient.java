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
     * Creates Kubernetes Pod.
     *
     * @param userName       name of the user who creates the pod
     * @param experimentName name of the experiment which should be deployed
     * @param executionName  name of the execution which should be deployed as a pod in kubernetes
     * @return the name of the pod in kubernetes
     * @throws ApiException if the communication with the api results in an error
     */
    String createPod(String userName, String experimentName, String executionName, UUID experimentId) throws ApiException;

    /**
     * Deletes Kubernetes Pod.
     *
     * @param userName       name of the user who is owner of the pod
     * @param experimentName name of the experiment where the execution is stored
     * @param podName        name of the pod which should be deleted from kubernetes
     * @throws ApiException if the communication with the api results in an error
     */
    void deletePod(@NonNull String userName, @NonNull String experimentName, @NonNull String podName) throws ApiException;

    /**
     * Retrieve the logs for a running Pod.
     *
     * @throws ApiException if the pod can't be deleted
     */
    String retrieveLogs(@NonNull String userName, @NonNull ExecutionDetails executionDetails, int lines, Integer sinceSeconds, boolean withTimestamps) throws ApiException;

}