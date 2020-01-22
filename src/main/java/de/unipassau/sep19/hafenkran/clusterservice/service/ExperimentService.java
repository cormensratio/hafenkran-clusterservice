package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public interface ExperimentService {

    /**
     * Persists a given {@link ExperimentDetails} in the experiment database.
     *
     * @param experimentDetails The {@link ExperimentDetails}, which are going to be persisted in the database.
     * @return The persisted {@link ExperimentDetails}.
     */
    ExperimentDetails createExperiment(@NonNull ExperimentDetails experimentDetails);

    /**
     * Returns the {@link ExperimentDTO} for the specified {@code id}.
     *
     * @param id The UUID of the requested {@link ExperimentDetails}.
     * @return The requested {@link ExperimentDetails} by its {@code id}.
     */
    ExperimentDTO retrieveExperimentDTOById(@NonNull UUID id);

    /**
     * Returns a list of {@link ExperimentDTO}s from the specified {@code userId}.
     *
     * @param userId The {@code userId} of the owner's {@link ExperimentDTOList}
     * @return The list of {@link ExperimentDTO}s of the requested {@code userId}.
     */
    List<ExperimentDTO> retrieveExperimentsDTOListOfUserId(@NonNull UUID userId);

    /**
     * Returns a list with all {@link ExperimentDTO}s stored in the database.
     *
     * @return The list with all {@link ExperimentDTO}s stored in the database.
     */
    List<ExperimentDTO> retrieveAllExperimentDTOs();

    /**
     * Shares an experiment and returns an {@link ExperimentDTO} with the permittedAccounts within.
     *
     * @param experimentId The id of the experiment to be shared.
     * @param userId       The id of the user, with whom the experiment should be shared.
     * @return The corresponding {@link ExperimentDTO}.
     */
    ExperimentDTO share(@NonNull UUID experimentId, @NonNull UUID userId);

    /**
     * Forbids the use of an experiment and returns an {@link ExperimentDTO} with the rest of the permittedAccounts.
     *
     * @param experimentId The id of the experiment which usage should be forbidden.
     * @param userId       The id of the user, for whom the experiment should be forbidden.
     * @return The corresponding {@link ExperimentDTO}.
     */
    ExperimentDTO forbid(@NonNull UUID experimentId, @NonNull UUID userId);

}
