package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.service.impl.ExperimentServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/experiments")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExperimentController {

    private final ExperimentServiceImpl experimentServiceImpl;

    @GetMapping("/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.FOUND)
    public ExperimentDTO getExperimentDTOById(@NotNull @PathVariable UUID id) {
        return experimentServiceImpl.getExperimentDTOById(id);
    }

    // TODO: get real userId
    @GetMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.FOUND)
    public List<ExperimentDTO> getExperimentDTOList() {
        return experimentServiceImpl.getExperimentsDTOListOfUserId(UUID.fromString("c8aef4f2-92f8-47eb-bbe9-bd457f91f0e6"));
    }
}