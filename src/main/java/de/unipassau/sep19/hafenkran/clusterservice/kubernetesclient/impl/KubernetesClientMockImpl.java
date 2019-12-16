package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl;

import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

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
    public void createNamespace(@NonNull ExperimentDetails experimentDetails) {
        log.info(String.format("KubernetesClientMockImpl: Creating namespace for %s", experimentDetails.getId()));
    }

    @Override
    public String createPod(@NonNull ExecutionDetails executionDetails) {
        log.info(String.format("KubernetesClientMockImpl: Creating pod for %s", executionDetails.getId()));
        return executionDetails.getName();
    }

    @Override
    public void deletePod(@NonNull ExecutionDetails executionDetails) {
        log.info(String.format("KubernetesClientMockImpl: Creating pod for %s", executionDetails.getId()));
    }

    @Override
    public String retrieveLogs(@NonNull ExecutionDetails executionDetails, int lines, Integer sinceSeconds, boolean withTimestamp) {
        log.info(String.format(
                "KubernetesClientMockImpl: Retrieving first %s lines printed to the log since %s for pod %s with id %s",
                lines, sinceSeconds, executionDetails.getPodName(), executionDetails.getId()));
        return String.format("this is a test log for %s \n 1 \n 2 \n 3", executionDetails.getPodName());
    }

    @Override
    public String retrieveResults(@NonNull ExecutionDetails executionDetails) {
        log.info(String.format("KubernetesClientMockImpl: Results retrieved from execution with id %s", executionDetails.getId()));
        File file = new File("mockResultsTar");
        try {
            StringBuilder sb = new StringBuilder("");
            Scanner sc = new Scanner(file);
            sc.forEachRemaining(sb::append);
            sc.close();
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void sendSTIN(@NonNull String input, @NonNull ExecutionDetails executionDetails) {
        log.info(String.format("KubernetesClientMockImpl: Sending the following input to execution with id %s: %s", executionDetails.getId(), input));
    }

}
