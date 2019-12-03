package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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

    @Value("${kubernetes.defaultLogLines}")
    private int defaultLogLines;

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
     * GET-Endpoint for receiving the logs of a running execution.
     *
     * @param executionId The UUID of the requested execution.
     * @return The logs of the running execution.
     */
    @GetMapping("/{executionId}/logs")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String getLogsDTOById(@NonNull @PathVariable UUID executionId,
                                 @RequestParam(value = "lines", required = false) Integer lines,
                                 @RequestParam(value = "sinceSeconds", required = false) Integer sinceSeconds,
                                 @RequestParam(value = "printTimestamps", defaultValue = "false") boolean printTimestamps) {
        if (lines == null || lines <= 0) {
            lines = defaultLogLines;
        }
        return executionService.retrieveLogsForExecutionId(executionId, lines, sinceSeconds, printTimestamps);
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

    @GetMapping(value = "/{executionId}/results", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public byte[] getResultsForExecution(@NonNull @PathVariable UUID executionId) {
        return executionService.getResults(executionId);
    }

}
