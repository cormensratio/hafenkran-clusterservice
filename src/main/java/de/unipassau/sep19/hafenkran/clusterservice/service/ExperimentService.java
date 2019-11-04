package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public interface ExperimentService {

    public ExperimentDetails createExperiment(@Valid ExperimentDetails experimentDetails);

    public ExperimentDetails getExperimentById(@NotNull @NonNull UUID id);

    public ExperimentDTO getExperimentDTOById(@NotNull UUID id);

    public List<ExperimentDetails> getExperimentsListOfUserId(@NotNull @NonNull UUID userId);

    public List<ExperimentDTO> getExperimentsDTOListOfUserId(@NotNull @NonNull UUID userId);
}
