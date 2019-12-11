package de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.models.V1Pod;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.InternalServerErrorException;

@Slf4j
@Component
public class PodEventHandler implements ResourceEventHandler<V1Pod> {

    @Autowired
    ExecutionService executionService;

    ExecutionDetails executionDetails;


    PodEventHandler(ExecutionDetails executionDetails){
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

    void setExecutionStatus(@NonNull V1Pod pod, @NonNull ExecutionDetails executionDetails) {

        /*
         Handles most status for a kubernetes pod. The "Failed" status is not handled because it is too imprecise
         for handling the needed status "CANCELED", "ABORTED" and "FAILED" for an execution.
        */
        switch (pod.getStatus().getPhase()) {
            case "Pending":
                executionDetails.setStatus(ExecutionDetails.Status.WAITING);
                break;
            case "Running":
                executionDetails.setStatus(ExecutionDetails.Status.RUNNING);
                break;
            case "Succeeded":
                executionDetails.setStatus(ExecutionDetails.Status.FINISHED);
                break;
            case "Unknown":
                throw new InternalServerErrorException(
                        String.format("The state of the pod \"%s\" in namespace \"%s\" could not be obtained!",
                                pod.getMetadata().getName(), pod.getMetadata().getNamespace()));
        }
    }
}
