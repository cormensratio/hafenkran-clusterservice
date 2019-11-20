package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient;

import java.io.IOException;

/**
 * Interface providing methods for interacting with a KubernetesClient.
 */
public interface KubernetesClient {

    /**
     * Initialise the KubernetesClient depending on the
     * property condition mockKubernetesClient.
     *
     * @throws IOException if the config file can't be found
     */
    void initKubeClient() throws IOException;

}
