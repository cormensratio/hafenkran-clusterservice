package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl;

import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import io.kubernetes.client.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
     *
     * @throws IOException Exception never thrown
     */
    public KubernetesClientMockImpl() throws IOException {
        log.info("Kubernetes Mock Client. Set mockKubernetesClient to false in application-dev.yml" +
                " if you want to use Kubernetes.");
    }

    @Override
    public String createPod(String userName, String experimentName, String executionName) throws ApiException {
        log.info("KubernetesClientMockImpl can not create a Pod.");
        return "No Pod created. KubernetesClientMockImpl.";
    }

    @Override
    public void deletePod(String userName, String experimentName, String podName) throws ApiException {
        log.info("KubernetesClientMockImpl can not delete a Pod.");
    }
}
