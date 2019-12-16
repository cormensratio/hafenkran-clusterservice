package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.util;

import de.unipassau.sep19.hafenkran.clusterservice.config.SpringContext;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.models.V1Pod;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * The EventHandler class for a kubernetes pod
 */
@Slf4j
public class PodEventHandler implements ResourceEventHandler<V1Pod> {

    @NonNull
    private final UUID executionId;

    @NonNull
    private ExecutionService executionService;

    public PodEventHandler(@NonNull UUID executionId) {
        this.executionId = executionId;
    }

    @Override
    public void onAdd(V1Pod pod) {
        log.debug(String.format("Pod \"%s\" added!", pod.getMetadata().getName()));
        log.debug(String.format("Namespace of pod with name \"%s\" is: \"%s\"\n",
                pod.getMetadata().getName(), pod.getMetadata().getNamespace()));
    }

    @Override
    public void onUpdate(V1Pod oldPod, V1Pod newPod) {
        setExecutionStatus(newPod, executionId);
        log.debug(String.format(
                "Pod with name \"%s\" and status \"%s\" updated to pod with name \"%s\" and status \"%s\"",
                oldPod.getMetadata().getName(), oldPod.getStatus().getPhase(),
                newPod.getMetadata().getName(), newPod.getStatus().getPhase()));
    }

    @Override
    public void onDelete(V1Pod pod, boolean deletedFinalStateUnknown) {
        setExecutionStatus(pod, executionId);
        log.debug(String.format("Pod with name \"%s\" has status \"%s\"",
                pod.getMetadata().getName(), pod.getStatus().getPhase()));
        log.debug(String.format("Pod with name \"%s\" deleted!\n", pod.getMetadata().getName()));
    }

    private ExecutionService getExecutionService() {
        return SpringContext.getBean(ExecutionService.class);
    }

    private void setExecutionStatus(@NonNull V1Pod pod, @NonNull UUID executionId) {
        executionService = getExecutionService();

        switch (pod.getStatus().getPhase()) {

            // Kubernetes status --> execution status
            // Pending --> WAITING
            case "Pending":
                executionService.changeExecutionStatus(executionId, ExecutionDetails.Status.WAITING);
                break;
            // Running --> RUNNING
            case "Running":
                executionService.changeExecutionStatus(executionId, ExecutionDetails.Status.RUNNING);
                break;
            // Succeeded --> FINISHED
            case "Succeeded":
                executionService.changeExecutionStatus(executionId, ExecutionDetails.Status.FINISHED);
                break;
            // Failed --> FAILED
            case "Failed":
                executionService.changeExecutionStatus(executionId, ExecutionDetails.Status.FAILED);
        }
    }
}
