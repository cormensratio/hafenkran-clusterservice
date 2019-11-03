package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.service.impl.ExperimentServiceImpl;
import de.unipassau.sep19.hafenkran.clusterservice.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("experiments")
public class ExperimentController {

    @Autowired
    private ExperimentServiceImpl experimentServiceImpl;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.FOUND)
    public ExperimentDTO getExperimentDTO(@NotNull @PathVariable Long id) {
        return experimentServiceImpl.findExperimentDTOById(id);
    }
}