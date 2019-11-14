package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
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
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutionServiceImpl implements ExecutionService {

    private final ExecutionRepository executionRepository;

    private final ExperimentRepository experimentRepository;

    public ExecutionDetails createExecutionFromExecDetails(@NonNull ExecutionDetails executionDetails) {

        final ExecutionDetails savedExecutionDetails =
                executionRepository.save(executionDetails);

        log.info(String.format("Execution with id %s created",
                savedExecutionDetails.getId()));

        return savedExecutionDetails;
    }

    public ExecutionDetails createExecutionFromExecDTO(@NonNull ExecutionCreateDTO executionCreateDTO) {

        final ExecutionDetails executionDetails =
                convertExecCreateDTOtoExecDetails(executionCreateDTO);

        final ExecutionDetails savedExecutionDetails =
                executionRepository.save(executionDetails);

        log.info(String.format("Execution with id %s created",
                savedExecutionDetails.getId()));

        return savedExecutionDetails;
    }

    public ExecutionDetails findExecutionById(@NonNull UUID id) {
        final Optional<ExecutionDetails> executionDetails =
                executionRepository.findById(id);

        return executionDetails.orElseThrow(() ->
                new ResourceNotFoundException(ExecutionDetails.class, "id", id.toString()));
    }

    public ExecutionDetails convertExecCreateDTOtoExecDetails(ExecutionCreateDTO execCreateDTO) {

        Optional<ExperimentDetails> byId =
                experimentRepository.findById(execCreateDTO.getExperimentId());
        return new ExecutionDetails(
                byId.get(),
                execCreateDTO.getName().get(),
                execCreateDTO.getRam().get(),
                execCreateDTO.getCpu().get(),
                execCreateDTO.getBookedTime().get()
        );
    }

    // TODO: 13.11.19
    public void startExecution(UUID executionId) {
    }
}
