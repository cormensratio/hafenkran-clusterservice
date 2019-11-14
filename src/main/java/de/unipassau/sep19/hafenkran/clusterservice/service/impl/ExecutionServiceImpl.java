package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExecutionRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutionServiceImpl implements ExecutionService {

    private final ExecutionRepository executionRepository;

    private List<ExecutionDetails> findExecutionsListOfExperimentId(@NonNull UUID experimentId) {
        return executionRepository.findAllByExperimentDetails_Id(experimentId);
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
    public ExecutionDetails findExecutionById(@NonNull UUID id) {
        final Optional<ExecutionDetails> executionDetails =
                executionRepository.findById(id);

        return executionDetails.orElseThrow(() ->
                new ResourceNotFoundException(ExecutionDetails.class, "id", id.toString()));
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
        return ExecutionDTOList.convertExecutionListToDTOList(findExecutionsListOfExperimentId(experimentId));
    }

}
