package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl;

import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Implementation of Kubernetes Mock Client for test purposes,
 * where Kubernetes isn't needed.
 */
@Slf4j
@Component
public class KubernetesClientMockImpl implements KubernetesClient {

    /**
     * Prints out info that mockKubernetesClient is used.
     *
     * @throws IOException Exception never thrown
     */
    @Override
    public void initKubeClient() throws IOException {
        log.info("Kubernetes Mock Client. Set mockKubernetesClient to false in application-dev.yml" +
                " if you want to use Kubernetes.");
    }
}
