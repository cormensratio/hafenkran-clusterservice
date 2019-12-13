package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.dto.StdinDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails.Status;
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
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Order(1)
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
    @Override
    public String retrieveLogsForExecutionId(@NonNull UUID id, int lines, Integer sinceSeconds, boolean withTimestamps) {
        final String logs;
        try {
            logs = kubernetesClient.retrieveLogs(retrieveExecutionDetailsById(id), lines, sinceSeconds,
                    withTimestamps);
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "There was an error while " +
                    "communicating with the cluster.", e);
        }
        return logs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionDTO createAndStartExecution(@NonNull ExecutionCreateDTO executionCreateDTO) {
        final ExecutionDetails executionDetails =
                createExecutionFromExecCreateDTO(executionCreateDTO);

        final ExecutionDetails createdExecutionDetails = createExecution(executionDetails);

        final ExecutionDetails startedExecutionDetails = startExecution(createdExecutionDetails);

        return ExecutionDTO.fromExecutionDetails(startedExecutionDetails);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionDetails createExecution(@NonNull ExecutionDetails executionDetails) {
        final ExecutionDetails savedExecutionDetails =
                executionRepository.save(executionDetails);

        log.info(String.format("Execution with id %s created",
                savedExecutionDetails.getId()));

        return savedExecutionDetails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionDTO terminateExecution(@NonNull UUID executionId) {

        ExecutionDetails executionDetails = getExecutionDetails(executionId);

        try {
            kubernetesClient.deletePod(executionDetails);
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "There was an error while "
                    + "communicating with the cluster.");
        }

        executionDetails.setStatus(Status.CANCELED);
        executionDetails.setTerminatedAt(LocalDateTime.now());

        executionRepository.save(executionDetails);

        ExecutionDTO terminatedExecutionDTO = ExecutionDTO.fromExecutionDetails(executionDetails);

        log.info(String.format("Execution with id %S terminated", executionId));

        return terminatedExecutionDTO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionDTO retrieveExecutionDTOById(@NonNull UUID id) {
        return ExecutionDTO.fromExecutionDetails(retrieveExecutionDetailsById(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public List<ExecutionDTO> retrieveExecutionsDTOListForUserId(@NonNull UUID userId) {
        List<ExecutionDetails> executionDetailsList = executionRepository.findAllByExperimentDetails_OwnerId(userId);

        if (executionDetailsList.isEmpty()) {
            return Collections.emptyList();
        }

        executionDetailsList.forEach(ExecutionDetails::validatePermissions);
        return ExecutionDTOList.fromExecutionDetailsList(executionDetailsList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ExecutionDTO deleteExecution(@NonNull UUID executionId) {

        ExecutionDetails executionDetails = getExecutionDetails(executionId);

        if (executionDetails.getStatus().equals(Status.RUNNING)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can not delete executions in running or waiting");
        }

        executionRepository.deleteById(executionId);
        log.info(String.format("Execution with id %S deleted", executionId));
        return ExecutionDTO.fromExecutionDetails(executionDetails);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getResults(@NonNull UUID executionId) {
        ExecutionDetails executionDetails = retrieveExecutionDetailsById(executionId);
        try {
            return kubernetesClient.retrieveResults(executionDetails).getBytes();
        } catch (ApiException | IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Results couldn't be found.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendSTDIN(@NonNull UUID executionId, @NonNull StdinDTO stdinDTO) {
        ExecutionDetails executionDetails = retrieveExecutionDetailsById(executionId);
        try {
            kubernetesClient.sendSTIN(stdinDTO.getInput(), executionDetails);
        } catch (IOException | ApiException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There was an error while " +
                    "communicating with the cluster.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeExecutionStatus(@NonNull UUID executionId, @NonNull Status status) {
        final ExecutionDetails executionDetails =
                executionRepository.findById(executionId).orElseThrow(
                        () -> new ResourceNotFoundException(ExecutionDetails.class, "id", executionId.toString()));

        // Do not change the status if the execution is already canceled or aborted
        if (executionDetails.getStatus().equals(Status.CANCELED)
                || executionDetails.getStatus().equals(Status.ABORTED)) {
            return;
        }

        executionDetails.setStatus(status);
        executionRepository.save(executionDetails);
    }

    private ExecutionDetails retrieveExecutionDetailsById(@NonNull UUID id) {
        final Optional<ExecutionDetails> execution = executionRepository.findById(id);
        ExecutionDetails executionDetails = execution.orElseThrow(
                () -> new ResourceNotFoundException(ExecutionDetails.class, "id",
                        id.toString()));
        executionDetails.validatePermissions();
        return executionDetails;
    }

    private ExecutionDetails startExecution(@NonNull ExecutionDetails executionDetails) {
        String podName = null;
        try {
            podName = kubernetesClient.createPod(executionDetails);
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "There was an error while "
                    + "communicating with the cluster.");
        }

        executionDetails.setPodName(podName);
        executionDetails.setStatus(Status.RUNNING);
        executionDetails.setStartedAt(LocalDateTime.now());

        executionRepository.save(executionDetails);

        return executionDetails;
    }

    private ExecutionDetails getExecutionDetails(@NonNull UUID executionId) {
        Optional<ExecutionDetails> executionDetailsById =
                executionRepository.findById(executionId);

        final ExecutionDetails execution = executionDetailsById.orElseThrow(
                () -> new ResourceNotFoundException(ExperimentDetails.class, "id",
                        executionId.toString()));

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

        String inputName;

        // Get name either from the execCreateDTO or from the experiment
        if (!execCreateDTO.getName().isPresent()) {
            if (experiment.getName().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Experimentname must be at least one alphanumeric letter.");
            }
            inputName = experiment.getName();
        } else {
            inputName = execCreateDTO.getName().get();
        }

        // Check if the name is containing the filetype and change the name if true
        if (inputName.contains(String.valueOf('.'))) {
            inputName = inputName.substring(0, inputName.indexOf('.'));
        }

        // Check if the name matches the regex
        String regex = "[a-z0-9]([-a-z0-9]*[a-z0-9])?";
        if (Pattern.matches(regex, inputName.toLowerCase())) {
            name = inputName + "-" + (experiment.getExecutionDetails().size() + 1);
        } else {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "You can only use alphanumeric letters and a hyphen for naming. "
                            + "Must start and end alphanumeric.");
        }

        // Set variables from the ExecutionDetails
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
