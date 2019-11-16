package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.service.UploadService;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * The REST-Controller for experiment specific POST and GET endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/experiments")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExperimentController {

    private final ExperimentService experimentService;

    private final UploadService uploadService;

    private final ExecutionService executionService;

    /**
     * GET-Endpoint for receiving a single {@link ExperimentDTO} by its id.
     *
     * @param experimentId The UUID of the requested {@link ExperimentDTO}.
     * @return The requested {@link ExperimentDTO} by its {@code id}.
     */
    @GetMapping("/{experimentId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ExperimentDTO getExperimentDTOById(@NonNull @PathVariable UUID experimentId) {
        return experimentService.findExperimentDTOById(experimentId);
    }

    /**
     * GET-Endpoint for receiving an {@link ExperimentDTOList} of current user.
     *
     * @return The list of {@link ExperimentDTO}s of the current user.
     */
    @GetMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<ExperimentDTO> getExperimentDTOListOfCurrentUser() {
        return experimentService.findExperimentsDTOListOfUserId(SecurityContextUtil.getCurrentUserDTO().getId());
    }

    /**
     * Creates a new experiment with an random UUID, the fileName as the experimentName and the fileSize as the
     * experimentSize, stores it and uploads the specified MultipartFile {@code file} to the created
     * experiment.
     *
     * @param file The file to be uploaded.
     * @return An ExperimentDTO from the new created and stored experiment.
     */
    @PostMapping("/uploadFile")
    public ExperimentDTO uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("name") String experimentName) {
        if (StringUtils.isEmpty(experimentName)) {
            experimentName = file.getOriginalFilename();
        }
        ExperimentDetails experimentDetails = new ExperimentDetails(SecurityContextUtil.getCurrentUserDTO().getId(),
                experimentName, file.getSize());
        ExperimentDetails experiment = experimentService.createExperiment(experimentDetails);

        return uploadService.storeFile(file, experiment);
    }

    /**
     * GET-Endpoint for receiving an {@link ExecutionDTOList} of the current experiment.
     *
     * @return The list of {@link ExecutionDTO}s of the current experiment.
     */
    @GetMapping("/{experimentId}/executions")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<ExecutionDTO> getExecutionDTOListForExperimentId(@PathVariable UUID experimentId) {
        return executionService.findExecutionsDTOListOfExperimentId(experimentId);
    }

    @PostMapping("/{experimentId}/execute")
    public @ResponseBody
    ExecutionDTO startExecution(
            @NonNull @RequestBody ExecutionCreateDTO executionCreateDTO) {

        return executionService.convertExecDetailsToExecDTO(executionService.createExecutionFromExecCreateDTO(executionCreateDTO));
    }


}
