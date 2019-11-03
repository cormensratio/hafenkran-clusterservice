package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.service.impl.ExperimentServiceImpl;
import de.unipassau.sep19.hafenkran.clusterservice.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("experiments")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExperimentController {

    private final ExperimentServiceImpl experimentServiceImpl;

    private final UserServiceImpl userServiceImpl;

    /*
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<ExperimentDTO> getExperiments() {
        List<ExperimentDetails> experiment = experimentServiceImpl.getAllExperimentDTOsOfUserId()
    }
     */

    @GetMapping("/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.FOUND)
    public ExperimentDTO getExperimentDTOById(@NotNull @PathVariable UUID id) {
        return experimentServiceImpl.findExperimentDTOById(id);
    }
}