package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient;

import java.util.UUID;

public interface KubernetesClient {

    String createPod(UUID experimentId, String executionName);

    String deletePod(UUID experimentId, String executionName);

}
