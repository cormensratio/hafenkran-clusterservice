package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.util;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.models.V1Pod;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.InternalServerErrorException;

@Slf4j
public class PodEventHandler implements ResourceEventHandler<V1Pod> {

    @NonNull
    private final ExecutionDetails executionDetails;

    @NonNull
    private ExecutionService executionService;

    public PodEventHandler(@NonNull ExecutionDetails executionDetails) {
        this.executionDetails = executionDetails;
    }

    @Override
    public void onAdd(V1Pod pod) {
        log.info(String.format("Pod \"%s\" added!", pod.getMetadata().getName()));
        log.info(String.format("Namespace of pod with name \"%s\" is: \"%s\"\n",
                pod.getMetadata().getName(), pod.getMetadata().getNamespace()));
    }

    @Override
    public void onUpdate(V1Pod oldPod, V1Pod newPod) {
        setExecutionStatus(newPod, executionDetails);
        log.info(String.format(
                "Pod with name \"%s\" and status \"%s\" updated to pod with name \"%s\" and status \"%s\"",
                oldPod.getMetadata().getName(), oldPod.getStatus().getPhase(),
                newPod.getMetadata().getName(), newPod.getStatus().getPhase()));
        System.out.println("__________");
        log.info(newPod.getStatus().getMessage());
        System.out.println("__________");

    }

    @Override
    public void onDelete(V1Pod pod, boolean deletedFinalStateUnknown) {
        setExecutionStatus(pod, executionDetails);
        log.info(String.format("Pod with name \"%s\" has status \"%s\"",
                pod.getMetadata().getName(), pod.getStatus().getPhase()));
        log.info(String.format("Pod with name \"%s\" deleted!\n", pod.getMetadata().getName()));
    }

    private ExecutionService getExecutionService() {
        return SpringContext.getBean(ExecutionService.class);
    }

    private void setExecutionStatus(@NonNull V1Pod pod, @NonNull ExecutionDetails executionDetails) {

        executionService = getExecutionService();

        switch (pod.getStatus().getPhase()) {

            // Kubernetes status --> execution status
            // Pending --> WAITING
            case "Pending":
                executionService.changeExecutionStatus(executionDetails.getId(), ExecutionDetails.Status.WAITING);
                break;
            // Running --> RUNNING
            case "Running":
                executionService.changeExecutionStatus(executionDetails.getId(), ExecutionDetails.Status.RUNNING);
                break;
            // Succeeded --> FINISHED
            case "Succeeded":
                executionService.changeExecutionStatus(executionDetails.getId(), ExecutionDetails.Status.FINISHED);
                break;
            // Failed --> FAILED
            case "Failed":
                executionService.changeExecutionStatus(executionDetails.getId(), ExecutionDetails.Status.FAILED);
            case "Unknown":
                throw new InternalServerErrorException(
                        String.format("The state of the pod \"%s\" in namespace \"%s\" could not be obtained!",
                                pod.getMetadata().getName(), pod.getMetadata().getNamespace()));
        }
    }
}
