package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.*;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails.Status;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExecutionRepository;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import de.unipassau.sep19.hafenkran.clusterservice.serviceclient.ReportingServiceClient;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import io.kubernetes.client.ApiException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutionServiceImpl implements ExecutionService {

    private final ExecutionRepository executionRepository;

    private final ExperimentRepository experimentRepository;

    private final KubernetesClient kubernetesClient;

    private final ReportingServiceClient rsClient;

    @Value("${kubernetes.deployment.defaults.ram}")
    private long ramDefault;

    @Value("${kubernetes.deployment.defaults.cpu}")
    private long cpuDefault;

    @Value("${kubernetes.deployment.defaults.bookedTime}")
    private long bookedTimeDefault;

    @Value("${kubernetes.mock.kubernetesClient}")
    private boolean mockKubernetesClient;

    /**
     * Automatically goes through all running pods in a fixed interval and terminates the execution
     * if the booked time was exceeded.
     */
    @Scheduled(fixedDelayString = "#{${kubernetes.pod-cleanup-scheduler-delay}*1000}")
    private void terminatePodsAfterBookedTimeExceeded() {
        if (mockKubernetesClient) {
            return;
        }

        List<ExecutionDetails> runningExecutions = executionRepository.findAllByStatus(Status.RUNNING);
        runningExecutions.forEach(e -> {
            if (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                    < e.getStartedAt().toEpochSecond(ZoneOffset.UTC) + e.getBookedTime()) {
                terminateExecution(e.getId(), true);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String retrieveLogsForExecutionId(@NonNull UUID id, int lines, Integer sinceSeconds, boolean withTimestamps) {
        ExecutionDetails executionDetails = retrieveExecutionDetailsById(id);

        if (!executionDetails.getStatus().equals(ExecutionDetails.Status.RUNNING)) {
            return "Logs can only be retrieved for running executions!";
        }

        final String logs;
        try {
            logs = kubernetesClient.retrieveLogs(executionDetails, lines, sinceSeconds,
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
    public ExecutionDTO terminateExecution(@NonNull UUID executionId, boolean skipPermissionValidation) {

        ExecutionDetails executionDetails = getExecutionDetails(executionId);

        if (!skipPermissionValidation) {
            executionDetails.validatePermissions();
        }

        try {
            kubernetesClient.deletePod(executionDetails);
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "There was an error while "
                    + "communicating with the cluster.");
        }
        UserDTO user = SecurityContextUtil.getCurrentUserDTO();

        if (user.isAdmin()
                && !user.getId().equals(executionDetails.getOwnerId())
                && !executionDetails.getStatus().equals(Status.CANCELED)
                && !executionDetails.getStatus().equals(Status.FINISHED)) {

            executionDetails.setStatus(ExecutionDetails.Status.ABORTED);

        } else if (!executionDetails.getStatus().equals(Status.ABORTED)
                && !executionDetails.getStatus().equals(Status.FINISHED)) {

            executionDetails.setStatus(ExecutionDetails.Status.CANCELED);
        }
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

    private ExecutionDetails retrieveExecutionDetailsById(@NonNull UUID id) {
        final Optional<ExecutionDetails> execution = executionRepository.findById(id);
        ExecutionDetails executionDetails = execution.orElseThrow(
                () -> new ResourceNotFoundException(ExecutionDetails.class, "id", id.toString()));
        executionDetails.validatePermissions();
        return execution.get();
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
        List<ExecutionDetails> executionDetailsList = executionRepository.findAllByOwnerId(userId);

        if (executionDetailsList.isEmpty()) {
            return Collections.emptyList();
        }

        executionDetailsList.forEach(ExecutionDetails::validatePermissions);
        return ExecutionDTOList.fromExecutionDetailsList(executionDetailsList);
    }

    /**
     * {@inheritDoc}
     */
    public List<ExecutionDTO> retrieveAllExecutionsDTOs() {
        List<ExecutionDetails> executionDetailsList = executionRepository.findAll();

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
    public byte[] getResults(@NonNull UUID executionId) {
        ExecutionDetails executionDetails = retrieveExecutionDetailsById(executionId);
        try {
            return Base64.getEncoder().encode(kubernetesClient.retrieveResults(executionDetails).getBytes());
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

        if (status.equals(Status.FINISHED)) {
            executionDetails.setTerminatedAt(LocalDateTime.now());
        }

        // Only change the status if the execution is RUNNING, WAITING or FAILED
        if (executionDetails.getStatus().equals(Status.RUNNING)
                || executionDetails.getStatus().equals(Status.WAITING)
                || executionDetails.getStatus().equals(Status.FAILED)) {
            executionDetails.setStatus(status);
            executionRepository.save(executionDetails);
        }
    }

    private ExecutionDetails startExecution(@NonNull ExecutionDetails executionDetails) {
        executionDetails.validatePermissions();

        final String podName;
        final boolean freeNamespaceResources;

        try {
            freeNamespaceResources = kubernetesClient.checkAvailableNamespaceResources(executionDetails);

            if (freeNamespaceResources) {
                podName = kubernetesClient.createPod(executionDetails);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The requested cpu and/or ram exceeds the limit.");
            }
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "There was an error while "
                    + "communicating with the cluster while starting the execution.");
        }


        executionDetails.setPodName(podName);
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

        experiment.setTotalNumberOfExecutionsStarted(experiment.getTotalNumberOfExecutionsStarted() + 1);

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
            name = inputName + "-" + (experiment.getTotalNumberOfExecutionsStarted());
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

    /**
     * {@inheritDoc}
     */
    public ExecutionDetails getExecutionOfPod(@NonNull String podName, @NonNull UUID namespace) {

        ExperimentDetails experiment = experimentRepository.findById(namespace).orElseThrow(
                () -> new ResourceNotFoundException(ExperimentDetails.class, "id", namespace.toString()));

        return executionRepository.findByPodNameAndExperimentDetails(podName, experiment);
    }

    @Override
    public void updatePersistedResults(@NonNull ExecutionDetails execution) {
        byte[] results = getResults(execution.getId());
        ResultDTO resultDTO = new ResultDTO(execution.getId(), execution.getOwnerId(), Base64.getEncoder().encodeToString(results));
        rsClient.sendResultsToResultsService(resultDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionDTO deleteExecution(@NonNull UUID executionId) {

        ExecutionDetails executionDetails = getExecutionDetails(executionId);

        executionDetails.validatePermissions();

        if (executionDetails.getStatus().equals(Status.RUNNING)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can not delete executions in running");
        }

        executionRepository.deleteById(executionId);
        log.info(String.format("Execution with id %S deleted", executionId));
        return ExecutionDTO.fromExecutionDetails(executionDetails);
    }
}
