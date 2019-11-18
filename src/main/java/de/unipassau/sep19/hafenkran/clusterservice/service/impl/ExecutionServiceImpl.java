package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExecutionRepository;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Value("${kubernetes.deployment.defaults.ram}")
    private long ramDefault;

    @Value("${kubernetes.deployment.defaults.cpu}")
    private long cpuDefault;

    @Value("${kubernetes.deployment.defaults.bookedTime}")
    private long bookedTimeDefault;

    private List<ExecutionDetails> findExecutionsListOfExperimentId(@NonNull UUID experimentId) {
        return executionRepository.findAllByExperimentDetails_Id(experimentId);
    }

    private List<ExecutionDetails> findExecutionsListForUserId(@NonNull UUID userId) {
        return executionRepository.findAllByExperimentDetails_UserId(userId);
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

    /**
     * {@inheritDoc}
     */
    public ExecutionDTO createExecution(@NonNull ExecutionCreateDTO executionCreateDTO) {

        final ExecutionDetails executionDetails =
                convertExecCreateDTOtoExecDetails(executionCreateDTO);

        final ExecutionDetails savedExecutionDetails =
                executionRepository.save(executionDetails);

        final ExecutionDTO executionDTO =
                convertExecDetailsToExecDTO(savedExecutionDetails);

        log.info(String.format("Execution with id %s created",
                savedExecutionDetails.getId()));

        return executionDTO;
    }

    public ExecutionDetails findExecutionById(@NonNull UUID id) {
        final Optional<ExecutionDetails> executionDetails =
                executionRepository.findById(id);

        return executionDetails.orElseThrow(() ->
                new ResourceNotFoundException(ExecutionDetails.class, "id", id.toString()));
    }

    /**
     * {@inheritDoc}
     */
    public ExecutionDTO convertExecDetailsToExecDTO(@NonNull ExecutionDetails execDetails) {
        return new ExecutionDTO(execDetails);
    }

    /**
     * {@inheritDoc}
     */
    public ExecutionDTO findExecutionDTOById(@NonNull UUID id) {
        final Optional<ExecutionDetails> execution = executionRepository.findById(id);

        return new ExecutionDTO(execution.orElseThrow(() -> new ResourceNotFoundException(ExecutionDetails.class, "id",
                id.toString())));
    }

    /**
     * {@inheritDoc}
     */
    public List<ExecutionDTO> findExecutionsDTOListOfExperimentId(@NonNull UUID experimentId) {
        List<ExecutionDetails> executionDetailsList = findExecutionsListOfExperimentId(experimentId);

        if (executionDetailsList.isEmpty()) {
            return Collections.emptyList();
        }
        return ExecutionDTOList.convertExecutionListToDTOList(executionDetailsList);
    }

    /**
     * {@inheritDoc}
     */
    public List<ExecutionDTO> findExecutionsDTOListForUserId(@NonNull UUID userId) {
        List<ExecutionDetails> executionDetailsList = findExecutionsListForUserId(userId);

        if (executionDetailsList.isEmpty()) {
            throw new ResourceNotFoundException(ExecutionDetails.class, "userId", userId.toString());
        }
        return ExecutionDTOList.convertExecutionListToDTOList(executionDetailsList);
    }

    private ExecutionDetails convertExecCreateDTOtoExecDetails(@NonNull ExecutionCreateDTO execCreateDTO) {
        Optional<ExperimentDetails> experimentDetailsbyId =
                experimentRepository.findById(execCreateDTO.getExperimentId());

        final ExperimentDetails experiment = experimentDetailsbyId.orElseThrow(
                () -> new ResourceNotFoundException(ExperimentDetails.class, "id", experimentDetailsbyId.get().getId().toString()));
        final String name;
        final long ram;
        final long cpu;
        final long bookedTime;

        if (!execCreateDTO.getName().isPresent()) {
            name = experiment.getExperimentName() + " #" + experiment.getExecutionDetailsList().size();
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

        return new ExecutionDetails(experiment, name, ram, cpu, bookedTime);
    }
}
