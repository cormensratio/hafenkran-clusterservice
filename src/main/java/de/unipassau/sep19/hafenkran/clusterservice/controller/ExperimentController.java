package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.*;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.service.UploadService;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
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

    @Value("${service-user.secret}")
    private String serviceSecret;

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
        return experimentService.retrieveExperimentDTOById(experimentId);
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
        if (SecurityContextUtil.getCurrentUserDTO().isAdmin()) {
            return experimentService.retrieveAllExperimentDTOs();
        } else {
            return experimentService.retrieveExperimentsDTOListOfUserId(SecurityContextUtil.getCurrentUserDTO().getId());
        }
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

        return uploadService.storeFile(file, experimentName);
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
        return executionService.retrieveExecutionsDTOListOfExperimentId(experimentId);
    }

    /**
     * POST-Endpoint for creating an {@link ExecutionDetails} and receiving its corresponding {@link ExecutionDTO}.
     *
     * @param executionCreateDTO The DTO representation of the execution that is going to be created.
     * @return The corresponding {@link ExecutionDTO}.
     */
    @PostMapping("/{experimentId}/execute")
    public @ResponseBody
    ExecutionDTO startExecution(@PathVariable UUID experimentId,
                                @NonNull @RequestBody @Valid ExecutionCreateDTO executionCreateDTO) {

        return executionService.createAndStartExecution(executionCreateDTO);
    }

    /**
     * POST-Endpoint for updating an experiment including the userAccess-Management.
     *
     * @param experimentId            The id of the experiment to be updated.
     * @param permittedUsersUpdateDTO The new options from the experiment, that should be updated (including userAccess).
     * @return The corresponding {@link ExperimentDTO}.
     */
    @PostMapping("/{experimentId}/permittedUsers")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ExperimentDTO updatePermittedUsers(@PathVariable UUID experimentId, @RequestBody PermittedUsersUpdateDTO permittedUsersUpdateDTO) {
        return experimentService.updatePermittedUsers(experimentId, permittedUsersUpdateDTO);
    }

    /**
     * POST-Endpoint for deleting experiments of the owner.
     *
     * @param ownerId The id of owner which experiments should be deleted.
     * @param secret  The permission for calling internal server endpoints.
     */
    @PostMapping("/delete")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void deleteExperimentsForOwnerId(@RequestParam(value = "ownerId", required = true) UUID ownerId,
                                            @RequestParam(value = "secret", required = true) @NotEmpty String secret) {
        if (!secret.equals(serviceSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You are not authorized to call an internal service endpoint");
        }
        experimentService.deleteExperimentsByOwnerId(ownerId);
    }

    /**
     * POST-Endpoint for deleting one experiment with the {@code experimentId}.
     * If the user, who is deleting the experiment, isn't the owner, only his access will be deleted.
     * Else the whole experiment will be deleted for everyone, including all executions.
     *
     * @param experimentId The id of the experiment to be deleted.
     */
    @PostMapping("/{experimentId}/delete}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void deleteExperiment(@PathVariable UUID experimentId) {
        experimentService.deleteExperimentById(experimentId);

    }

}
