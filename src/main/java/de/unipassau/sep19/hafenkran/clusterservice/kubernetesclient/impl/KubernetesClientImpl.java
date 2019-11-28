package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl;

import com.google.gson.JsonSyntaxException;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.PodLogs;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of Kubernetes Java Client to communicate with the cluster via api.
 */
@Slf4j
@Component
public class KubernetesClientImpl implements KubernetesClient {

    private CoreV1Api api;

    @Value("${dockerHubUser}")
    private String dockerHubUser;

    @Value("${dockerHubPassword}")
    private String dockerHubPassword;

    @Value("${dockerHubEmail}")
    private String dockerHubEmail;

    @Value("${dockerHubKey}")
    private String dockerHubKey;

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
        String image = "hafenkran/hafenkran-repo:c3fbebea-6ac8-4297-b9e6-256f23ff25fc";
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
            createNamespacedImagePullSecret(namespaceString);
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
        if (getAllNamespacedPods(namespaceString).size() == 1 && getAllNamespacedPods(namespaceString).contains(podName)) {
            try {
                deleteNamespace(namespaceString);
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
        } else {
            try {
                deleteNamespacedPod(namespaceString, podName);
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
    }

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

    private List<String> getAllNamespaces() throws ApiException {
        V1NamespaceList listNamespace =
                api.listNamespace(true, "pretty", null, null, null, 0, null, Integer.MAX_VALUE, Boolean.FALSE);
        return listNamespace
                .getItems()
                .stream()
                .map(v1Namespace -> v1Namespace.getMetadata().getName())
                .collect(Collectors.toList());
    }

    private List<String> getAllNamespacedPods(@NonNull String namespaceString) throws ApiException {
        V1PodList podList =
                api.listNamespacedPod(namespaceString, true, "pretty", null, null, null, 0, null, Integer.MAX_VALUE, Boolean.FALSE);
        return podList
                .getItems()
                .stream()
                .map(v1Pod -> v1Pod.getMetadata().getName())
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

        V1Container container = new V1ContainerBuilder()
                .withName(podName)
                .withImage(image)
                .withImagePullPolicy("IfNotPresent")
                .withNewStdin(true)
                .withTty(true)
                .build();

        V1LocalObjectReference secret = new V1LocalObjectReferenceBuilder()
                .withName("newsecret")
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
                .withImagePullSecrets(secret)
                .withHostNetwork(true)
                .endSpec()
                .build();
        api.createNamespacedPod(namespaceString, pod, true, "pretty", null);
        log.info("Created pod {}", podName);
    }

    private void createNamespacedImagePullSecret(@NonNull String namespaceString) throws ApiException {
        V1Secret newSecret = new V1SecretBuilder()
                .withNewMetadata()
                .withName("newsecret")
                .withNamespace(namespaceString)
                .endMetadata()
                .build();
        newSecret.setType("kubernetes.io/dockerconfigjson");
        String dockerCfg = String.format("{\"auths\": {\"%s\": {\"username\": \"%s\",\t\"password\": \"%s\",\"email\": \"%s\",\t\"auth\": \"%s\"}}}",
                "https://index.docker.io/v1/",
                dockerHubUser,
                dockerHubPassword,
                dockerHubEmail,
                dockerHubKey);
        Map<String, byte[]> data = new HashMap<>();
        data.put(".dockerconfigjson", dockerCfg.getBytes());
        newSecret.setData(data);
        api.createNamespacedSecret(namespaceString, newSecret, true, "pretty", null);
        log.info("Created namespacedSecret {} in Namespace {}", newSecret.getMetadata().getName(), namespaceString);
    }

    private void deleteNamespace(@NonNull String namespaceString) throws ApiException {
        V1DeleteOptions deleteOptions = new V1DeleteOptions();
        api.deleteNamespace(namespaceString, "pretty", deleteOptions, null, null, null, null);
        log.info("Deleted namespace {}", namespaceString);
    }

    private void deleteNamespacedPod(@NonNull String namespaceString, @NonNull String podName) throws ApiException {
        V1DeleteOptions deleteOptions = new V1DeleteOptions();
        api.deleteNamespacedPod(podName, namespaceString, "pretty", deleteOptions, null, null, null, null);
        log.info("Deleted pod {}", podName);
    }
}