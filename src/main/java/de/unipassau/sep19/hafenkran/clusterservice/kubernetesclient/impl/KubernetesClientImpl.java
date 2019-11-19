package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl;

import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Implementation of Kubernetes Java Client to communicate with the cluster via api.
 */
@Slf4j
@Component
public class KubernetesClientImpl implements KubernetesClient {

    @Getter
    private CoreV1Api api;

    /**
     * Auto detects kubernetes config files to connect to the client and sets
     * up the api to access the cluster.
     *
     * @throws IOException if the config file can't be found
     */
    @Override
    public void initKubeClient() throws IOException {
        log.info("Kubernetes Client ready!");
        //auto detect kubernetes config file
        ApiClient client = Config.defaultClient();
        //set global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);
        //the CoreV1Api loads default api-client from global configuration
        api = new CoreV1Api(client);
    }
}
