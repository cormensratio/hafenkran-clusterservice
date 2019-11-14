package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/experiments")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping("/{experimentId}/execute")
    public @ResponseBody
    ExecutionDTO startExecution(
            @NonNull @RequestBody ExecutionCreateDTO executionCreateDTO) {

        ExecutionDetails executionDetails =
                executionService.createExecutionFromExecDTO(executionCreateDTO);

        //TODO: update executionDetails with kubernetes information

        return new ExecutionDTO(executionDetails);
    }
}
