package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;

public interface ExperimentService {

    public ExperimentDetails createExperiment(@Valid ExperimentDetails experimentDetails);

    public ExperimentDetails findExperimentById(@NotNull @NonNull UUID id);

    public ExperimentDTO findExperimentDTOById(@NotNull UUID id);

}
