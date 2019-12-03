package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl;

import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
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

    @Override
    public String createPod(@NonNull UUID experimentId, @NonNull String executionName) {
        log.info("KubernetesClientMockImpl: Creating pod with name " + executionName);
        return "No Pod created. KubernetesClientMockImpl.";
    }

    @Override
    public void deletePod(@NonNull UUID experimentId, @NonNull String executionName) {
        log.info("KubernetesClientMockImpl: Deleting pod for execution " + executionName);
    }

    @Override
    public String retrieveLogs(@NonNull ExecutionDetails executionDetails, int lines, Integer sinceSeconds, boolean withTimestamp) {
        log.info(String.format(
                "KubernetesClientMockImpl: Retrieving first %s lines printed to the log since %s for pod %s with id %s",
                lines, sinceSeconds, executionDetails.getPodName(), executionDetails.getId()));
        return String.format("this is a test log for %s \n 1 \n 2", executionDetails.getPodName());
    }

}
