package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExecutionRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/experiments")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutionController {

    private final ExecutionService executionService;

    private final ExperimentService experimentService;

    @PostMapping("/{experimentId}/execute")
    public ExecutionDTO startExecution(@NonNull @PathVariable UUID experimentId) {

        ExperimentDetails experiment =
                experimentService.findExperimentById(experimentId);
        ExecutionDetails execution = new ExecutionDetails(experiment);
        ExecutionDetails savedExecution =
                executionService.createExecution(execution);
        ExecutionDTO executionDTO = new ExecutionDTO()
        return new
    }
}
