package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
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
    public ExecutionDetails createExecutionFromExecCreateDTO(@NonNull ExecutionCreateDTO executionCreateDTO) {

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

    public ExecutionDTO convertExecDetailsToExecDTO(@NonNull ExecutionDetails execDetails) {
        return new ExecutionDTO(execDetails);
    }

    private ExecutionDetails convertExecCreateDTOtoExecDetails(@NonNull ExecutionCreateDTO execCreateDTO) {
        Optional<ExperimentDetails> experimentDetailsbyId =
                experimentRepository.findById(execCreateDTO.getExperimentId());

        ExecutionDetails executionDetails = new ExecutionDetails();
        final ExperimentDetails experiment = experimentDetailsbyId.orElseThrow(() -> new ResourceNotFoundException(ExperimentDetails.class, "id", experimentDetailsbyId.get().getId().toString()));
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
            ram = executionDetails.getRamDefault();
        } else {
            ram = execCreateDTO.getRam().get();
        }

        if (!execCreateDTO.getCpu().isPresent() || execCreateDTO.getCpu().get() <= 0) {
            cpu = executionDetails.getCpuDefault();
        } else {
            cpu = execCreateDTO.getCpu().get();
        }

        if (!execCreateDTO.getBookedTime().isPresent() || execCreateDTO.getBookedTime().get() <= 0) {
            bookedTime = executionDetails.getBookedTimeDefault();
        } else {
            bookedTime = execCreateDTO.getBookedTime().get();
        }

        return new ExecutionDetails(experiment, name, ram, cpu, bookedTime);
    }
}
