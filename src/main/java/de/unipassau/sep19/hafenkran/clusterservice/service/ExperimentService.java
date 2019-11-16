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
    ExperimentDetails createExperiment(@Valid ExperimentDetails experimentDetails);

    /**
     * Returns the {@link ExperimentDetails} from the specified {@code id}.
     *
     * @param id The UUID of the requested {@link ExperimentDetails}.
     * @return The requested {@link ExperimentDetails} by its {@code id}.
     */
    ExperimentDetails findExperimentById(@NonNull UUID id);

    /**
     * Returns the {@link ExperimentDTO} from the specified {@code id}.
     *
     * @param id The UUID of the requested {@link ExperimentDTO}.
     * @return The requested {@link ExperimentDTO} by its {@code id}.
     */
    ExperimentDTO findExperimentDTOById(@NonNull UUID id);

    /**
     * Returns a list of {@link ExperimentDTO}s from the specified {@code userId}.
     *
     * @param userId The {@code userId} of the owner's {@link ExperimentDTOList}
     * @return The list of {@link ExperimentDTO}s of the requested {@code userId}.
     */
    List<ExperimentDTO> findExperimentsDTOListOfUserId(@NonNull UUID userId);

}
