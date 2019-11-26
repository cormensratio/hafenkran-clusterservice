package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import de.unipassau.sep19.hafenkran.clusterservice.service.ReportingService;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * The REST-Controller for execution specific POST and GET endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/executions")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutionController {

    private final ExecutionService executionService;

    private final ReportingService reportingService;

    /**
     * GET-Endpoint for receiving a single {@link ExecutionDTO} by its id.
     *
     * @param executionId The UUID of the requested {@link ExecutionDTO}.
     * @return The requested {@link ExecutionDTO} by its {@code id}.
     */
    @GetMapping("/{executionId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ExecutionDTO getExecutionDTOById(@NonNull @PathVariable UUID executionId) {
        return executionService.retrieveExecutionDTOById(executionId);
    }

    /**
     * GET-Endpoint for receiving an {@link ExecutionDTOList} of the current experiment.
     *
     * @return The list of {@link ExecutionDTO}s of the current experiment.
     */
    @GetMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<ExecutionDTO> getExecutionDTOListForCurrentId() {
        return executionService.retrieveExecutionsDTOListForUserId(SecurityContextUtil.getCurrentUserDTO().getId());
    }

    /**
     * POST-Endpoint for terminating an execution from an experiment.
     *
     * @param executionId The id from the execution, which should be terminated.
     * @return An {@link ExecutionDTO} from the changed execution.
     */
    @PostMapping("/{executionId}/cancel")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ExecutionDTO terminateExecution(@NonNull @PathVariable UUID executionId) {
        return executionService.terminateExecution(executionId);
    }

    @GetMapping("/{executionId}/persistentresults")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public File getPersistentResultsForExecution(@NonNull @PathVariable UUID executionId) {
        return reportingService.getPersistentResults(executionId);
    }

}
