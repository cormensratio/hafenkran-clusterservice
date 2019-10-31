package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public interface ExperimentService {

    public ExperimentDetails createExperiment(@Valid ExperimentDetails experimentDetails);

    public ExperimentDetails findExperimentById(@NotNull Long id);

    public ExperimentDTO findExperimentDTOById(@NotNull Long id);

}
