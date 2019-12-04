package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import io.kubernetes.client.ApiException;
import lombok.NonNull;

import java.io.IOException;
import java.util.UUID;

/**
 * Interface providing methods for interacting with a KubernetesClient.
 */
public interface KubernetesClient {

    /**
     * Creates Kubernetes Pod.
     *
     * @param experimentId  Id of the experiment where the execution is stored.
     * @param executionName The name of the execution which should be deployed as a pod in kubernetes.
     * @return The name of the pod in kubernetes.
     * @throws ApiException if the communication with the api results in an error.
     */
    String createPod(@NonNull UUID experimentId, @NonNull String executionName) throws ApiException;

    /**
     * Deletes Kubernetes Pod.
     *
     * @param experimentId  id of the experiment where the execution is stored
     * @param executionName the name of the execution which pod should be deleted from kubernetes.
     * @throws ApiException if the communication with the api results in an error
     */
    void deletePod(@NonNull UUID experimentId, @NonNull String executionName) throws ApiException;

    /**
     * Retrieves the logs of the execution, but only if the given execution is currently running.
     *
     * @param executionDetails    The id of the target execution.
     * @param lines          The amount of lines to be returned.
     * @param sinceSeconds   The time in seconds defining the range from where to start the extraction of logs.
     * @param withTimestamps Show the timestamp for every line.
     * @return The string with the lines from the log.
     * @throws ApiException if the pod can't be found.
     */
    String retrieveLogs(@NonNull ExecutionDetails executionDetails, int lines, Integer sinceSeconds, boolean withTimestamps) throws ApiException;

    /**
     * Sending an standard-{@code input} to the kubernetes container.
     *
     * @param input            The input to be sent.
     * @param executionDetails The pod to receive the inputs.
     * @throws IOException  if the input isn't viable.
     * @throws ApiException if pod couldn't be found.
     */
    void sendSTIN(@NonNull String input, @NonNull ExecutionDetails executionDetails) throws IOException, ApiException;

}