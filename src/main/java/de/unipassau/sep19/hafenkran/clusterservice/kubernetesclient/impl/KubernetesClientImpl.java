package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl;

import com.google.gson.JsonSyntaxException;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.util.PodEventHandler;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import io.kubernetes.client.*;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.CallGeneratorParams;
import io.kubernetes.client.util.Config;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.ws.rs.InternalServerErrorException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of Kubernetes Java Client to communicate with the cluster via api.
 */
@Slf4j
@Component
public class KubernetesClientImpl implements KubernetesClient {

    private CoreV1Api api;

    private SharedInformerFactory factory;

    private SharedIndexInformer<V1Pod> podInformer;

    @Value("${dockerHubRepoPath}")
    private String DOCKER_HUB_REPO_PATH;

    @Value("${dockerClient.username}")
    private String dockerClientUsername;

    @Value("${dockerClient.password}")
    private String dockerClientPassword;

    @Value("${dockerClient.email}")
    private String dockerClientEmail;

    @Value("${dockerClient.authKey}")
    private String dockerClientAuthKey;

    @Value("${kubernetes.debugging}")
    private boolean debugMode;

    @Value("${kubernetes.config.path}")
    private String kubernetesConfigLocation;

    @Value("${kubernetes.config.load-default}")
    private boolean loadDefaultConfig;

    /**
     * Constructor of KubernetesClientImpl.
     * <p>
     * Auto detects kubernetes config files to connect to the client and sets
     * up the api to access the cluster.
     */
    public KubernetesClientImpl() {

    }

    /**
     * Due to the manual initialization of the KubernetesClient in the ConfigEntrypoint the @Value marked fields are
     * only injected after the construction, which means that all config related fields are null during the construction
     * of the class.
     */
    @PostConstruct
    private void postConstruct() throws IOException {
        // load kubernetes config file
        final ApiClient client = loadDefaultConfig
                ? Config.defaultClient()
                : Config.fromConfig(kubernetesConfigLocation);

        // debugging must be set to false for pod informer
        client.setDebugging(debugMode);
        client.getHttpClient().setReadTimeout(0, TimeUnit.SECONDS);

        // set global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);

        // the CoreV1Api loads default api-client from global configuration
        api = new CoreV1Api(client);
        log.info("Kubernetes Client ready!");

        factory = new SharedInformerFactory();
        //createAndStartPodInformer();
        log.info("Kubernetes Pod informer ready!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNamespace(@NonNull ExperimentDetails experimentDetails) throws ApiException {
        String namespace = experimentDetails.getId().toString();

        List<String> allNamespaces = getAllNamespaces();

        if (!allNamespaces.contains(namespace)) {
            createNamespace(namespace);
            createImagePullSecretForNamespace(namespace);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createPod(@NonNull ExecutionDetails executionDetails) throws ApiException {
        String namespace = executionDetails.getExperimentDetails().getId().toString();
        String checksum = executionDetails.getExperimentDetails().getChecksum();
        String image = DOCKER_HUB_REPO_PATH + ":" + checksum;
        String podName = executionDetails.getName();

        Map<String, String> labels = new HashMap<>();
        labels.put("run", podName);
        createPodInNamespace(namespace, podName, image, labels);
        return api.readNamespacedPod(podName, namespace, "pretty", false, false).getMetadata().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePod(@NonNull ExecutionDetails executionDetails) throws ApiException {
        String namespace = getNamespace(executionDetails);
        String podName = getPodName(executionDetails);

        List<String> allPodsInNamespace = getAllPodsFromNamespace(namespace);

        if (allPodsInNamespace == null) {
            throw new IllegalArgumentException("This namespace doesnt exist");
        }
        try {
            if (allPodsInNamespace.contains(podName)) {
                deletePodInNamespace(namespace, podName);
            }
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
        executionDetails.validatePermissions();

        if (!executionDetails.getStatus().equals(ExecutionDetails.Status.RUNNING)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Found execution for id %s, but with status %s.", executionDetails.getId(),
                            executionDetails.getStatus()));
        }

        final String namespace = getNamespace(executionDetails);
        final String podName = getPodName(executionDetails);

        return api.readNamespacedPodLog(podName, namespace, null, false, null, "pretty", false, sinceSeconds,
                lines,
                withTimestamps);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String retrieveResults(@NonNull ExecutionDetails executionDetails) throws ApiException, IOException {
        String namespace = getNamespace(executionDetails);
        String podName = getPodName(executionDetails);
        Exec exec = new Exec();

        final Process proc =
                exec.exec(
                        namespace,
                        podName,
                        new String[]{"sh", "-c", "tar cf - " + "/results" + " | base64"},
                        null,
                        false,
                        false);

        final String output;
        try (InputStream is = new Base64InputStream(new BufferedInputStream(proc.getInputStream()))) {
            output = IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not retrieve results for the given pod");
        }

        return output;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendSTIN(@NonNull String input, @NonNull ExecutionDetails executionDetails) throws IOException, ApiException {
        if (!executionDetails.getStatus().equals(ExecutionDetails.Status.RUNNING)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Found execution for id %s, but with status %s.", executionDetails.getId(),
                            executionDetails.getStatus()));
        }

        String namespace = getNamespace(executionDetails);
        String podName = getPodName(executionDetails);

        Attach attach = new Attach();
        final Attach.AttachResult result = attach.attach(namespace, podName, true);
        OutputStream output = result.getStandardInputStream();

        output.write(input.getBytes());
        output.write('\n');
        output.flush();
        output.close();
        result.close();
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

    private List<String> getAllPodsFromNamespace(@NonNull String namespace) throws ApiException {
        V1PodList podList =
                api.listNamespacedPod(namespace, true, "pretty", null, null, null, 0, null, Integer.MAX_VALUE,
                        Boolean.FALSE);
        return podList
                .getItems()
                .stream()
                .map(v1Pod -> v1Pod.getMetadata().getName())
                .collect(Collectors.toList());
    }

    private void createNamespace(@NonNull String namespace) throws ApiException {
        V1Namespace experimentNamespace = new V1NamespaceBuilder()
                .withNewMetadata()
                .withName(namespace)
                .endMetadata()
                .build();
        api.createNamespace(experimentNamespace, true, "pretty", null);
        log.info("Created namespace {}", namespace);

    }

    /**
     * Creates a Kubernetes Pod and sets the Image Pull Secret for it.
     *
     * @param namespace namespace where the pod should be created
     * @param podName   name of the pod
     * @param image     name of the image which should be used
     * @param labels    list of labels which should be used
     * @throws ApiException if the communication with the api results in an error
     */
    private void createPodInNamespace(@NonNull String namespace, @NonNull String podName, @NonNull String
            image,
                                      @NonNull Map<String, String> labels) throws ApiException {

        V1Container container = new V1ContainerBuilder()
                .withName(podName)
                .withImage(image)
                .withImagePullPolicy("IfNotPresent")
                .withNewStdin(true)
                .withTty(true)
                .build();

        V1LocalObjectReference imagePullSecret = new V1LocalObjectReferenceBuilder()
                .withName("image-pull-secret") //references secret with the given name
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
                .withRestartPolicy("Never")
                .withImagePullSecrets(imagePullSecret)//sets the secret for accessing docker registry
                .withHostNetwork(true)
                .endSpec()
                .build();
        api.createNamespacedPod(namespace, pod, true, "pretty", null);
        log.info("Created pod {} in Namespace {}", podName, namespace);
    }

    /**
     * Creates Image Pull Secret for a Namespace. All created Pods in the same namespace can access it.
     *
     * @param namespace namespace where the secret should be created
     * @throws ApiException if the communication with the api results in an error
     */
    private void createImagePullSecretForNamespace(@NonNull String namespace) throws ApiException {
        V1Secret imagePullSecret = new V1SecretBuilder()
                .withNewMetadata()
                .withName("image-pull-secret")
                .withNamespace(namespace)
                .endMetadata()
                .build();
        imagePullSecret.setType("kubernetes.io/dockerconfigjson");
        String dockerCfg = String.format(
                "{\"auths\": {\"%s\": {\"username\": \"%s\",\t\"password\": \"%s\",\"email\": \"%s\",\t\"auth\": \"%s\"}}}",
                "https://index.docker.io/v1/",
                dockerClientUsername,
                dockerClientPassword,
                dockerClientEmail,
                dockerClientAuthKey);
        Map<String, byte[]> data = new HashMap<>();
        data.put(".dockerconfigjson", dockerCfg.getBytes());
        imagePullSecret.setData(data);
        api.createNamespacedSecret(namespace, imagePullSecret, true, "pretty", null);
        log.info("Created Image-Pull-Secret {} for Namespace {}", imagePullSecret.getMetadata().getName(),
                namespace);
    }

    private void deleteNamespace(@NonNull String namespace) throws ApiException {
        V1DeleteOptions deleteOptions = new V1DeleteOptions();
        api.deleteNamespace(namespace, "pretty", deleteOptions, null, null, null, null);
        log.info("Deleted namespace {}", namespace);
    }

    private String getNamespace(@NonNull ExecutionDetails executionDetails) {
        return executionDetails.getExperimentDetails().getId().toString();
    }

    private String getPodName(@NonNull ExecutionDetails executionDetails) {
        return executionDetails.getPodName();
    }

    private void deletePodInNamespace(@NonNull String namespace, @NonNull String podName) throws ApiException {
        V1DeleteOptions deleteOptions = new V1DeleteOptions();
        api.deleteNamespacedPod(podName, namespace, "pretty", deleteOptions, null, null, null, null);
        log.info("Deleted pod {}", podName);
    }


    private void createAndStartPodInformer() {

        if (podInformer != null) {
            return;
        }

        podInformer =
                factory.sharedIndexInformerFor(
                        (CallGeneratorParams params) -> {
                            try {
                                return api.listPodForAllNamespacesCall(
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        params.resourceVersion,
                                        params.timeoutSeconds,
                                        params.watch,
                                        null,
                                        null);

                            } catch (ApiException e) {
                                throw new InternalServerErrorException("An error occurred while retrieving status " +
                                        "updates for experiments.", e);
                            }
                        },
                        V1Pod.class,
                        V1PodList.class);

        podInformer.addEventHandler(new PodEventHandler());
        factory.startAllRegisteredInformers();
    }

}
