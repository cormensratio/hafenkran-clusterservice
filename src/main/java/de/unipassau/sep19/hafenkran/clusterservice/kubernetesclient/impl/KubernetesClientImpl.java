package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl;

import com.google.gson.JsonSyntaxException;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import io.kubernetes.client.*;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.informer.ResourceEventHandler;
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

import javax.ws.rs.InternalServerErrorException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of Kubernetes Java Client to communicate with the cluster via api.
 */
@Slf4j
@Component
public class KubernetesClientImpl implements KubernetesClient {

    private CoreV1Api api;

    private SharedInformerFactory factory = new SharedInformerFactory();

    @Value("${dockerHubRepoPath}")
    private String DOCKER_HUB_REPO_PATH;

    @Value("${dockerRegistry.username}")
    private String dockerRegistryUsername;

    @Value("${dockerRegistry.password}")
    private String dockerRegistryPassword;

    @Value("${dockerRegistry.email}")
    private String dockerRegistryEmail;

    @Value("${dockerRegistry.authKey}")
    private String dockerRegistryAuthKey;

    /**
     * Constructor of KubernetesClientImpl.
     * <p>
     * Auto detects kubernetes config files to connect to the client and sets up the api to access the cluster.
     *
     * @throws IOException if the config file can't be found
     */
    public KubernetesClientImpl() throws IOException {
        log.info("Kubernetes Client ready!");
        //auto detect kubernetes config file
        ApiClient client = Config.defaultClient();
        client.setDebugging(false);
        //set global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);
        //the CoreV1Api loads default api-client from global configuration
        api = new CoreV1Api(client);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createPod(@NonNull ExecutionDetails executionDetails) throws ApiException {
        String namespaceString = executionDetails.getExperimentDetails().getId().toString();
        String image = DOCKER_HUB_REPO_PATH + ":" + executionDetails.getExperimentDetails().getId();
        String podName = executionDetails.getName();

        List<String> allNamespaces = getAllNamespaces();
        if (! allNamespaces.contains(namespaceString)) {
            createNamespace(namespaceString);
            createImagePullSecretForNamespace(namespaceString);
        }
        Map<String, String> labels = new HashMap<>();
        labels.put("run", podName);
        createPodInNamespace(namespaceString, podName, image, labels);
        createPodInformerForNamespace(namespaceString);
        return api.readNamespacedPod(podName, namespaceString, "pretty", false, false).getMetadata().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePod(@NonNull ExecutionDetails executionDetails) throws ApiException {
        String namespaceString = executionDetails.getExperimentDetails().getId().toString();
        String podName = executionDetails.getPodName();

        List<String> allPodsInNamespace = getAllPodsFromNamespace(namespaceString);

        if (allPodsInNamespace == null) {
            throw new IllegalArgumentException("This namespace doesnt exist");
        }
        try {
            if (allPodsInNamespace.size() <= 1 && allPodsInNamespace.contains(podName)) {
                deleteNamespace(namespaceString);
            } else {
                deletePodInNamespace(namespaceString, podName);
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

        if (! executionDetails.getStatus().equals(ExecutionDetails.Status.RUNNING)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Found execution for id %s, but with status %s.", executionDetails.getId(),
                            executionDetails.getStatus()));
        }

        final String namespace = executionDetails.getExperimentDetails().getId().toString();
        final String podName = executionDetails.getPodName();

        return api.readNamespacedPodLog(podName, namespace, null, false, null, "pretty", false, sinceSeconds,
                lines,
                withTimestamps);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String retrieveResults(@NonNull ExecutionDetails executionDetails) throws ApiException, IOException {
        String namespace = executionDetails.getExperimentDetails().getId().toString();
        String podName = executionDetails.getPodName();
        Exec exec = new Exec();

        final Process proc =
                exec.exec(
                        namespace,
                        podName,
                        new String[]{"sh", "-c", "tar cf - " + "/results" + " | base64"},
                        null,
                        false,
                        false);

        InputStream is = new Base64InputStream(new BufferedInputStream(proc.getInputStream()));
        return IOUtils.toString(is, StandardCharsets.UTF_8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendSTIN(@NonNull String input, @NonNull ExecutionDetails executionDetails) throws IOException, ApiException {
        String namespace = executionDetails.getExperimentDetails().getId().toString();
        String podName = executionDetails.getPodName();

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

    private List<String> getAllPodsFromNamespace(@NonNull String namespaceString) throws ApiException {
        V1PodList podList =
                api.listNamespacedPod(namespaceString, true, "pretty", null, null, null, 0, null, Integer.MAX_VALUE,
                        Boolean.FALSE);
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

    /**
     * Creates a Kubernetes Pod and sets the Image Pull Secret for it.
     *
     * @param namespaceString namespace where the pod should be created
     * @param podName         name of the pod
     * @param image           name of the image which should be used
     * @param labels          list of labels which should be used
     * @throws ApiException if the communication with the api results in an error
     */
    private void createPodInNamespace(@NonNull String namespaceString, @NonNull String podName, @NonNull String
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
        api.createNamespacedPod(namespaceString, pod, true, "pretty", null);
        log.info("Created pod {} in Namespace {}", podName, namespaceString);
    }

    /**
     * Creates Image Pull Secret for a Namespace. All created Pods in the same namespace can access it.
     *
     * @param namespaceString namespace where the secret should be created
     * @throws ApiException if the communication with the api results in an error
     */
    private void createImagePullSecretForNamespace(@NonNull String namespaceString) throws ApiException {
        V1Secret imagePullSecret = new V1SecretBuilder()
                .withNewMetadata()
                .withName("image-pull-secret")
                .withNamespace(namespaceString)
                .endMetadata()
                .build();
        imagePullSecret.setType("kubernetes.io/dockerconfigjson");
        String dockerCfg = String.format(
                "{\"auths\": {\"%s\": {\"username\": \"%s\",\t\"password\": \"%s\",\"email\": \"%s\",\t\"auth\": \"%s\"}}}",
                "https://index.docker.io/v1/",
                dockerRegistryUsername,
                dockerRegistryPassword,
                dockerRegistryEmail,
                dockerRegistryAuthKey);
        Map<String, byte[]> data = new HashMap<>();
        data.put(".dockerconfigjson", dockerCfg.getBytes());
        imagePullSecret.setData(data);
        api.createNamespacedSecret(namespaceString, imagePullSecret, true, "pretty", null);
        log.info("Created Image-Pull-Secret {} for Namespace {}", imagePullSecret.getMetadata().getName(),
                namespaceString);
    }

    private void deleteNamespace(@NonNull String namespaceString) throws ApiException {
        V1DeleteOptions deleteOptions = new V1DeleteOptions();
        api.deleteNamespace(namespaceString, "pretty", deleteOptions, null, null, null, null);
        log.info("Deleted namespace {}", namespaceString);
    }

    private void deletePodInNamespace(@NonNull String namespaceString, @NonNull String podName) throws ApiException {
        V1DeleteOptions deleteOptions = new V1DeleteOptions();
        api.deleteNamespacedPod(podName, namespaceString, "pretty", deleteOptions, null, null, null, null);
        log.info("Deleted pod {}", podName);
    }

    private void createPodInformerForNamespace(@NonNull String namespace) {
        SharedIndexInformer<V1Pod> podInformer =
                factory.sharedIndexInformerFor(
                        (CallGeneratorParams params) -> {
                            try {
                                return api.listNamespacedPodCall(
                                        namespace,
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
        podInformer.addEventHandler(
                new ResourceEventHandler<V1Pod>() {
                    @Override
                    public void onAdd(V1Pod pod) {
                        System.out.printf("%s pod added!\n", pod.getMetadata().getName());
                    }

                    @Override
                    public void onUpdate(V1Pod oldPod, V1Pod newPod) {
                        System.out.printf(
                                "%s => %s pod updated!\n",
                                oldPod.getMetadata().getName(), newPod.getMetadata().getName());
                    }

                    @Override
                    public void onDelete(V1Pod pod, boolean deletedFinalStateUnknown) {
                        System.out.printf("%s pod deleted!\n", pod.getMetadata().getName());
                    }
                });

        factory.startAllRegisteredInformers();
/*
        V1Node nodeToCreate = new V1Node();
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName("noxu");
        nodeToCreate.setMetadata(metadata);
        try {
            V1Node createdNode = api.createNode(nodeToCreate, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Lister<V1Node> nodeLister = new Lister<V1Node>(nodeInformer.getIndexer());
        V1Node node = nodeLister.get("noxu");
        System.out.printf("noxu created! %s\n", node.getMetadata().getCreationTimestamp());
        factory.stopAllRegisteredInformers();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("informer stopped..");
 */
    }
}
