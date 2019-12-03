package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl;

import com.google.gson.JsonSyntaxException;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import io.kubernetes.client.*;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of Kubernetes Java Client to communicate with the cluster via api.
 */
@Slf4j
@Component
public class KubernetesClientImpl implements KubernetesClient {

    private CoreV1Api api;

    @Value("${resultsStorageLocation}")
    private String path;

    /**
     * Constructor of KubernetesClientImpl.
     * <p>
     * Auto detects kubernetes config files to connect to the client and sets
     * up the api to access the cluster.
     *
     * @throws IOException if the config file can't be found
     */
    public KubernetesClientImpl() throws IOException {
        log.info("Kubernetes Client ready!");
        //auto detect kubernetes config file
        ApiClient client = Config.defaultClient();
        client.setDebugging(true);
        //set global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);
        //the CoreV1Api loads default api-client from global configuration
        api = new CoreV1Api(client);
    }

    /**
     * Creates Kubernetes Pod.
     *
     * @param experimentId  id of the experiment where the execution is stored
     * @param executionName the name of the execution which should be deployed as a pod in kubernetes
     * @return the name of the pod in kubernetes
     * @throws ApiException if the communication with the api results in an error
     */
    @Override
    public String createPod(@NonNull UUID experimentId, @NonNull String executionName) throws ApiException {
        String namespaceString = experimentId.toString();
        String image = "martinjl/examples:1.0";
        String podName = executionName.toLowerCase();

        if (namespaceString.isEmpty()) {
            throw new IllegalArgumentException("Namespace is empty");
        }
        if (podName.isEmpty()) {
            throw new IllegalArgumentException("Podname is empty");
        }
        List<String> allNamespaces = getAllNamespaces();
        if (!allNamespaces.contains(namespaceString)) {
            createNamespace(namespaceString);
        }
        Map<String, String> labels = new HashMap<>();
        labels.put("run", podName);
        createKubernetesPod(namespaceString, podName, image, labels);
        return api.readNamespacedPod(podName, namespaceString, "pretty", false, false).getMetadata().getName();
    }

    /**
     * Deletes Kubernetes Pod.
     *
     * @param experimentId  id of the experiment where the execution is stored
     * @param executionName the name of the execution which pod should be deleted from kubernetes.
     * @throws ApiException if the communication with the api results in an error
     */
    @Override
    public void deletePod(@NonNull UUID experimentId, @NonNull String executionName) throws ApiException {
        String namespaceString = experimentId.toString();
        String podName = executionName.toLowerCase();

        if (namespaceString.isEmpty()) {
            throw new IllegalArgumentException("Namespace is empty.");
        }
        if (podName.isEmpty()) {
            throw new IllegalArgumentException("Podname is empty");
        }
        try {
            V1DeleteOptions deleteOptions = new V1DeleteOptions();
            api.deleteNamespacedPod(podName, namespaceString, "pretty", deleteOptions, null, null, null, null);
            log.info("Deleted pod {}", podName);
        } catch (JsonSyntaxException e) {
            if (e.getCause() instanceof IllegalStateException) {
                IllegalStateException ise = (IllegalStateException) e.getCause();
                if (ise.getMessage() != null && ise.getMessage().contains("Expected a string but" +
                        " was BEGIN_OBJECT"))
                    log.debug("Catching exception because of issue " +
                            "https://github.com/kubernetes-client/java/issues/86", e);
                else throw e;
            } else throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String retrieveLogs(@NonNull ExecutionDetails executionDetails, int lines, Integer sinceSeconds, boolean withTimestamps) throws ApiException {

        if (!executionDetails.getStatus().equals(ExecutionDetails.Status.RUNNING)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Found execution for id %s, but with status %s.", executionDetails.getId(),
                            executionDetails.getStatus()));
        }

        final String namespace = executionDetails.getExperimentDetails().getId().toString();
        final String podName = executionDetails.getPodName();

        final PodLogs logs = new PodLogs();

        InputStream is = null;
        try {
            is = logs.streamNamespacedPodLog(namespace, podName, null, sinceSeconds, lines, withTimestamps);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (is == null) {
            return "";
        }

        final Reader r = new InputStreamReader(is, StandardCharsets.UTF_8);
        final BufferedReader br = new BufferedReader(r);
        return br.lines().collect(Collectors.joining("\n"));


    }

    @Override
    public Path retrieveResults(@NonNull ExecutionDetails executionDetails) throws ApiException, IOException {
        Copy copy = new Copy();

        String namespace = executionDetails.getExperimentDetails().getId().toString();
        String podName = executionDetails.getPodName();

        // Configure exact naming of resultStorageLocation-path
        Path resultStorageLocation = Paths.get(String.format("%s/%s", path, executionDetails.getId())).toAbsolutePath().normalize();

        copy.copyDirectoryFromPod(api.readNamespacedPod(podName, namespace, "pretty", null, null), podName, resultStorageLocation);

        return resultStorageLocation;
    }

    private List<String> getAllNamespaces() throws ApiException {
        V1NamespaceList listNamespace =
                api.listNamespace(true, "pretty", null, null, null, 0, null, Integer.MAX_VALUE, Boolean.FALSE);
        return listNamespace
                .getItems()
                .stream()
                .map(v1Namespace -> v1Namespace.getMetadata().getName())
                .collect(Collectors.toList());
    }

    private void createNamespace(@NonNull String namespaceString) throws ApiException {
        V1Namespace experimentNamespace = new V1NamespaceBuilder()
                .withNewMetadata()
                .withName(namespaceString)
                .endMetadata()
                .build();
        api.createNamespace(experimentNamespace, true, "pretty", null);
        log.info("Created namespace {}", namespaceString);

    }

    private void createKubernetesPod(@NonNull String namespaceString, @NonNull String podName, @NonNull String
            image,
                                     @NonNull Map<String, String> labels) throws ApiException {
        V1ContainerPort containerPort = new V1ContainerPort();
        containerPort.containerPort(5000);
        V1Container container = new V1ContainerBuilder()
                .withName(podName)
                .withImage(image)
                .withImagePullPolicy("IfNotPresent")
                .withPorts(containerPort)
                .build();
        V1Pod pod = new V1PodBuilder()
                .withApiVersion("v1")
                .withKind("Pod")
                .withNewMetadata()
                .withName(podName)
                .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                .withContainers(container)
                .endSpec()
                .build();
        api.createNamespacedPod(namespaceString, pod, true, "pretty", null);
        log.info("Created pod {}", podName);
    }
}