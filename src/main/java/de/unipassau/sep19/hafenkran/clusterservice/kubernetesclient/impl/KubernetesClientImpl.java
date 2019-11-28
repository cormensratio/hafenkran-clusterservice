package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl;

import com.google.gson.JsonSyntaxException;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerBuilder;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1NamespaceBuilder;
import io.kubernetes.client.models.V1NamespaceList;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodBuilder;
import io.kubernetes.client.util.Config;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
     * {@inheritDoc}
     */
    public String createPod(@NonNull String userName, @NonNull String experimentName, @NonNull String executionName) throws ApiException {
        String experimentNameSubstring = experimentName.toLowerCase().substring(0, experimentName.lastIndexOf('.'));
        String namespaceString = userName.toLowerCase() + "-" + experimentNameSubstring;
        String image = "martinjl/examples:1.0";
        String podName = executionName.toLowerCase();

        if (userName.isEmpty()) {
            throw new IllegalArgumentException("Username is empty");
        }
        if (experimentNameSubstring.isEmpty()) {
            throw new IllegalArgumentException("Experimentname is empty");
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
     * {@inheritDoc}
     */
    public void deletePod(@NonNull String userName, String experimentName, @NonNull String podName) throws ApiException {
        String experimentNameSubstring = experimentName.toLowerCase().substring(0, experimentName.lastIndexOf('.'));
        String namespaceString = userName.toLowerCase() + "-" + experimentNameSubstring;

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

    private void createKubernetesPod(@NonNull String namespaceString, @NonNull String podName, @NonNull String image,
                                     @NonNull Map<String, String> labels) throws ApiException {
        V1Container container = new V1ContainerBuilder()
                .withName(podName)
                .withImage(image)
                .withImagePullPolicy("IfNotPresent")
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