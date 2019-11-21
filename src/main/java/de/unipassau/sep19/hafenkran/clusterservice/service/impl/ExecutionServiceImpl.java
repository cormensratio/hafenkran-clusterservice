package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExecutionRepository;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import io.kubernetes.client.ApiException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutionServiceImpl implements ExecutionService {

    private final ExecutionRepository executionRepository;

    private final ExperimentRepository experimentRepository;

    private final KubernetesClient kubernetesClient;

    @Value("${kubernetes.deployment.defaults.ram}")
    private long ramDefault;

    @Value("${kubernetes.deployment.defaults.cpu}")
    private long cpuDefault;

    @Value("${kubernetes.deployment.defaults.bookedTime}")
    private long bookedTimeDefault;

    /**
     * {@inheritDoc}
     */
    public ExecutionDTO createExecution(@NonNull ExecutionCreateDTO executionCreateDTO) throws ApiException {
        final ExecutionDetails executionDetails =
                createExecutionFromExecCreateDTO(executionCreateDTO);

        final ExecutionDetails startedExecutionDetails = startExecution(executionDetails);

        return ExecutionDTO.fromExecutionDetails(createExecution(startedExecutionDetails));
    }

    /**
     * {@inheritDoc}
     */
    public ExecutionDetails createExecution(@NonNull ExecutionDetails executionDetails) {
        final ExecutionDetails savedExecutionDetails =
                executionRepository.save(executionDetails);

        log.info(String.format("Execution with id %s created",
                savedExecutionDetails.getId()));

        return savedExecutionDetails;
    }

    private ExecutionDetails startExecution(@NonNull ExecutionDetails executionDetails) throws ApiException {
        String podName;

        try {
            podName = kubernetesClient.createPod(executionDetails.getExperimentDetails().getId(),
                    executionDetails.getName());
        } catch (ApiException e) {
            throw new ApiException();
        }

        executionDetails.setPodName(podName);
        executionDetails.setStatus(ExecutionDetails.Status.RUNNING);
        executionDetails.setStartedAt(LocalDateTime.now());

        return executionDetails;
    }

    public ExecutionDTO terminateExecution(@NonNull UUID executionId) throws ApiException {

        ExecutionDTO executionDTO = retrieveExecutionDTOById(executionId);

        ExecutionDetails executionDetails = getExecutionDetailsFromDTO(executionDTO);

        try {
            kubernetesClient.deletePod(executionDetails.getExperimentDetails().getId(),
                    executionDetails.getName());
        } catch (ApiException e) {
            throw new ApiException();
        }

        executionDetails.setPodName("");
        executionDetails.setStatus(ExecutionDetails.Status.CANCELED);
        executionDetails.setTerminatedAt(LocalDateTime.now());

        ExecutionDTO terminatedExecutionDTO = ExecutionDTO.fromExecutionDetails(executionDetails);

        log.info(String.format("Execution with id %S terminated", executionId));

        return terminatedExecutionDTO;
    }

    /**
     * {@inheritDoc}
     */
    public ExecutionDTO retrieveExecutionDTOById(@NonNull UUID id) {
        final Optional<ExecutionDetails> execution = executionRepository.findById(id);
        ExecutionDetails executionDetails = execution.orElseThrow(
                () -> new ResourceNotFoundException(ExecutionDetails.class, "id",
                        id.toString()));
        executionDetails.validatePermissions();
        return ExecutionDTO.fromExecutionDetails(executionDetails);
    }

    /**
     * {@inheritDoc}
     */
    public List<ExecutionDTO> retrieveExecutionsDTOListOfExperimentId(@NonNull UUID experimentId) {
        List<ExecutionDetails> executionDetailsList = executionRepository.findAllByExperimentDetails_Id(experimentId);

        if (executionDetailsList.isEmpty()) {
            return Collections.emptyList();
        }

        executionDetailsList.forEach(ExecutionDetails::validatePermissions);
        return ExecutionDTOList.fromExecutionDetailsList(executionDetailsList);
    }

    /**
     * {@inheritDoc}
     */
    public List<ExecutionDTO> retrieveExecutionsDTOListForUserId(@NonNull UUID userId) {
        List<ExecutionDetails> executionDetailsList = executionRepository.findAllByExperimentDetails_OwnerId(userId);

        if (executionDetailsList.isEmpty()) {
            return Collections.emptyList();
        }

        executionDetailsList.forEach(ExecutionDetails::validatePermissions);
        return ExecutionDTOList.fromExecutionDetailsList(executionDetailsList);
    }

    private ExecutionDetails getExecutionDetailsFromDTO(@NonNull ExecutionDTO executionDTO) {
        Optional<ExecutionDetails> executionDetailsById =
                executionRepository.findById(executionDTO.getId());

        final ExecutionDetails execution = executionDetailsById.orElseThrow(
                () -> new ResourceNotFoundException(ExperimentDetails.class, "id",
                        executionDTO.getExperimentId().toString()));

        execution.validatePermissions();
        return execution;
    }

    private ExecutionDetails createExecutionFromExecCreateDTO(@NonNull ExecutionCreateDTO execCreateDTO) {
        Optional<ExperimentDetails> experimentDetailsById =
                experimentRepository.findById(execCreateDTO.getExperimentId());

        final ExperimentDetails experiment = experimentDetailsById.orElseThrow(
                () -> new ResourceNotFoundException(ExperimentDetails.class, "id",
                        execCreateDTO.getExperimentId().toString()));

        experiment.validatePermissions();

        final String name;
        final long ram;
        final long cpu;
        final long bookedTime;

        if (!execCreateDTO.getName().isPresent()) {
            name = experiment.getName() + " #" + (experiment.getExecutionDetails().size() + 1);
        } else {
            name = execCreateDTO.getName().get();
        }

        if (!execCreateDTO.getRam().isPresent() || execCreateDTO.getRam().get() <= 0) {
            ram = ramDefault;
        } else {
            ram = execCreateDTO.getRam().get();
        }

        if (!execCreateDTO.getCpu().isPresent() || execCreateDTO.getCpu().get() <= 0) {
            cpu = cpuDefault;
        } else {
            cpu = execCreateDTO.getCpu().get();
        }

        if (!execCreateDTO.getBookedTime().isPresent() || execCreateDTO.getBookedTime().get() <= 0) {
            bookedTime = bookedTimeDefault;
        } else {
            bookedTime = execCreateDTO.getBookedTime().get();
        }

        return new ExecutionDetails(SecurityContextUtil.getCurrentUserDTO().getId(), experiment, name, ram, cpu,
                bookedTime);
    }
}
