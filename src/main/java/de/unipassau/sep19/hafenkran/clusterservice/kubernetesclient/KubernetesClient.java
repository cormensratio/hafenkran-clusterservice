package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import io.kubernetes.client.ApiException;
import lombok.NonNull;

import java.io.IOException;

/**
 * Interface providing methods for interacting with a KubernetesClient.
 */
public interface KubernetesClient {

    /**
     * Creates a new namespace for an Experiment.
     *
     * @param experimentDetails the details of the new experiment
     * @throws ApiException if the communication with the api results in an error
     */
    void createNamespace(@NonNull ExperimentDetails experimentDetails) throws ApiException;

    /**
     * Creates Kubernetes Pod.
     *
     * @param executionDetails the details of the new execution
     * @return the name of the pod in kubernetes
     * @throws ApiException if the communication with the api results in an error
     */
    String createPod(@NonNull ExecutionDetails executionDetails) throws ApiException;

    /**
     * Deletes Kubernetes Pod.
     *
     * @param executionDetails the details of the execution
     * @throws ApiException if the communication with the api results in an error
     */
    void deletePod(@NonNull ExecutionDetails executionDetails) throws ApiException;

    /**
     * Retrieves the logs of the execution, but only if the given execution is currently running.
     *
     * @param executionDetails The id of the target execution.
     * @param lines            The amount of lines to be returned.
     * @param sinceSeconds     The time in seconds defining the range from where to start the extraction of logs.
     * @param withTimestamps   Show the timestamp for every line.
     * @return The string with the lines from the log.
     * @throws ApiException if the pod can't be found.
     */
    String retrieveLogs(@NonNull ExecutionDetails executionDetails, int lines, Integer sinceSeconds, boolean withTimestamps) throws ApiException;

    /**
     * Retrieves the results of the execution from the pod in Kubernetes.
     *
     * @param executionDetails The execution to get the results from.
     * @return A Base64-String of the results.
     * @throws ApiException if the pod couldn't be found.
     * @throws IOException  if the input couldn't be read.
     */
    String retrieveResults(@NonNull ExecutionDetails executionDetails) throws ApiException, IOException;

    /**
     * Sending an standard-{@code input} to the kubernetes container.
     *
     * @param input            The input to be sent.
     * @param executionDetails The pod to receive the inputs.
     * @throws IOException  if the input isn't viable.
     * @throws ApiException if pod couldn't be found.
     */
    void sendSTIN(@NonNull String input, @NonNull ExecutionDetails executionDetails) throws IOException, ApiException;

    void deleteNamespace(@NonNull String namespace) throws ApiException;

    /**
     * Checks if the requested limits for cpu and ram are available on the namespace.
     *
     * @param executionDetails The execution with the requested cpu and ram limits.
     * @return False if the requested limits are available on the namespace, else true if the namespace resources are
     * already allocated.
     * @throws ApiException if the resource quota cant be found.
     */
    boolean checkIfNamespaceResourcesAlreadyAllocated(@NonNull ExecutionDetails executionDetails) throws ApiException;

    /**
     * Checks if there is enough node capacity the create a new namespace with resource quotas.
     * Checks if the used resources on a node + requested resources are lower than the node capacity.
     *
     * @param nodeName   The name of the node which should be checked.
     * @param usedCpu    The already used cpu of the node.
     * @param usedMemory The already used memory of the node.
     * @return True if there is enough free capacity on that node.
     * @throws ApiException if the node cant be found.
     */
    boolean checkIfEnoughNodeCapacityFree(@NonNull String nodeName, @NonNull long usedCpu,
                                          @NonNull long usedMemory) throws ApiException;
}