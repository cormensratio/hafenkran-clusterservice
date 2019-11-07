package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.service.impl.ExperimentServiceImpl;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    private static final UUID MOCK_ID = UUID.fromString("c8aef4f2-92f8-47eb-bbe9-bd457f91f0e6");
    private final ExperimentServiceImpl experimentServiceImpl;

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
        return experimentServiceImpl.findExperimentDTOById(experimentId);
    }

    /**
     * GET-Endpoint for receiving an {@link ExperimentDTOList} of current user.
     *
     * @return The list of {@link ExperimentDTO}s of the current user.
     */
    // TODO: get real userId
    @GetMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<ExperimentDTO> getExperimentDTOListOfCurrentUser() {
        return experimentServiceImpl.findExperimentsDTOListOfUserId(MOCK_ID);
    }
}