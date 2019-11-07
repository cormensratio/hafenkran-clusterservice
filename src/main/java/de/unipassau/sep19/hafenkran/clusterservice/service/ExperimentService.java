package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.NonNull;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

public interface ExperimentService {

    /**
     * Persists a given {@link ExperimentDetails} in the experiment database.
     *
     * @param experimentDetails The {@link ExperimentDetails}, which are going to be persisted in the database.
     * @return The persisted {@link ExperimentDetails}.
     */
    public ExperimentDetails createExperiment(@Valid ExperimentDetails experimentDetails);

    /**
     * @param id The UUID of the requested {@link ExperimentDetails}.
     * @return The requested {@link ExperimentDetails} by its {@code id}.
     */
    public ExperimentDetails findExperimentById(@NonNull UUID id);

    /**
     * @param id The UUID of the requested {@link ExperimentDTO}.
     * @return The requested {@link ExperimentDTO} by its {@code id}.
     */
    public ExperimentDTO findExperimentDTOById(@NonNull UUID id);

    /**
     * @param userId The {@code userId} of the owner's {@link ExperimentDTOList}
     * @return The list of {@link ExperimentDTO}s of the requested {@code userId}.
     */
    public List<ExperimentDTO> findExperimentsDTOListOfUserId(@NonNull UUID userId);
}
