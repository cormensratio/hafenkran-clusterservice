package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * The REST-Controller for execution specific POST and GET endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/experiments")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutionController {

    private static final UUID MOCK_ID = UUID.fromString("c8aef4f2-92f8-47eb-bbe9-bd457f91f0e6");

    private static UUID MOCK_EXPERIMENT_ID;
    //private static final String MOCK_EXPERIMENT_NAME = "ColdFusionAlgorithm";

    private final ExecutionService executionService;

    private final ExperimentService experimentService;

    private void initialiseMockExperimentId() {
        List<ExperimentDTO> experimentDTOList = experimentService.findExperimentsDTOListOfUserId(MOCK_ID);
        MOCK_EXPERIMENT_ID = experimentDTOList.get(0).getId();
    }

    /**
     * GET-Endpoint for receiving a single {@link ExecutionDTO} by its id.
     *
     * @param executionId The UUID of the requested {@link ExecutionDTO}.
     * @return The requested {@link ExecutionDTO} by its {@code id}.
     */
    @GetMapping("/{experimentId}/executions/{executionId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ExecutionDTO getExecutionDTOById(@NonNull @PathVariable UUID executionId) {
        return executionService.findExecutionDTOById(executionId);
    }

    /**
     * GET-Endpoint for receiving an {@link ExecutionDTOList} of the current experiment.
     *
     * @return The list of {@link ExecutionDTO}s of the current experiment.
     */
    // TODO: get real experimentId
    @GetMapping("/executions")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<ExecutionDTO> getExecutionDTOListOfCurrentExperiment() {
        initialiseMockExperimentId();
        return executionService.findExecutionsDTOListOfExperimentId(MOCK_EXPERIMENT_ID);
    }

}
