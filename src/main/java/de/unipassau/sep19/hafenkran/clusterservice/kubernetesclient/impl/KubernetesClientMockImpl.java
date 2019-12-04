package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl;

import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import io.kubernetes.client.ApiException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementation of Kubernetes Mock Client for test purposes,
 * where Kubernetes isn't needed.
 */
@Slf4j
@Component
public class KubernetesClientMockImpl implements KubernetesClient {

    /**
     * Constructor of KubernetesClientMockImpl.
     * <p>
     * Prints out info that mockKubernetesClient is used.
     */
    public KubernetesClientMockImpl() {
        log.info("Using KubernetesMockClient: Set mockKubernetesClient to false in application-dev.yml" +
                " if you want to use Kubernetes.");
    }

    /**
     * {@inheritDoc}
     */
    public String createPod(String userName, String experimentName, String executionName, UUID experimentId) throws ApiException {
        log.info("KubernetesClientMockImpl can not create a Pod.");
        return "No Pod created. KubernetesClientMockImpl.";
    }

    /**
     * {@inheritDoc}
     */
    public void deletePod(String userName, String experimentName, String podName) throws ApiException {
        log.info("KubernetesClientMockImpl can not delete a Pod.");
    }

    /**
     * {@inheritDoc}
     */
    public String retrieveLogs(@NonNull String userName, @NonNull ExecutionDetails executionDetails, int lines, Integer sinceSeconds, boolean withTimestamp) {
        log.info(String.format(
                "KubernetesClientMockImpl: Retrieving first %s lines printed to the log since %s for pod %s with id %s from user %s",
                lines, sinceSeconds, executionDetails.getPodName(), executionDetails.getId(), userName));
        return String.format("this is a test log for %s \n 1 \n 2", executionDetails.getPodName());
    }

}
