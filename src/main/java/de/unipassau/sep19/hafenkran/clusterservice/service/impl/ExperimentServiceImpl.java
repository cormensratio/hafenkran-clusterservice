package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExecutionRepository;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides {@link ExperimentDetails}, {@link ExperimentDTO} and {@link ExperimentDTOList} specific services.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExperimentServiceImpl implements ExperimentService {

    private final ExperimentRepository experimentRepository;

    private final ExecutionRepository executionRepository;

    private List<ExperimentDetails> findExperimentsListOfUserId(@NonNull UUID userId) {
        List<ExperimentDetails> experimentDetailsByUserId = experimentRepository.findExperimentDetailsByOwnerIdOrPermittedAccountsContaining(userId, userId);
        experimentDetailsByUserId.forEach(ExperimentDetails::validatePermissions);
        return experimentDetailsByUserId;
    }

    private List<ExperimentDetails> findAllExperiments() {
        List<ExperimentDetails> allExperimentsList = experimentRepository.findAll();
        allExperimentsList.forEach(ExperimentDetails::validatePermissions);
        return allExperimentsList;
    }

    /**
     * {@inheritDoc}
     */
    public ExperimentDetails createExperiment(@Valid @NonNull ExperimentDetails experimentDetails) {
        List<ExperimentDetails> foundExperiments = experimentRepository.findExperimentDetailsByOwnerIdAndName(
                experimentDetails.getOwnerId(), experimentDetails.getName());

        if (foundExperiments.size() == 0) {
            final ExperimentDetails savedExperimentDetails = experimentRepository.save(experimentDetails);
            log.info(String.format("Experiment with id %s created", savedExperimentDetails.getId()));
            return savedExperimentDetails;
        } else {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Experimentname: "
                    + experimentDetails.getName() + " already used. Must be unique.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public ExperimentDTO retrieveExperimentDTOById(@NonNull UUID id) {
        final Optional<ExperimentDetails> experimentDetailsOptional = experimentRepository.findById(id);
        ExperimentDetails experimentDetails = experimentDetailsOptional.orElseThrow(
                () -> new ResourceNotFoundException(ExperimentDetails.class, "id",
                        id.toString()));
        experimentDetails.validatePermissions();
        return ExperimentDTO.fromExperimentDetails(experimentDetails);
    }

    /**
     * {@inheritDoc}
     */
    public List<ExperimentDTO> retrieveExperimentsDTOListOfUserId(@NonNull UUID userId) {
        return ExperimentDTOList.convertExperimentListToDTOList(findExperimentsListOfUserId(userId));
    }

    /**
     * {@inheritDoc}
     */
    public List<ExperimentDTO> retrieveAllExperimentDTOs() {
        return ExperimentDTOList.convertExperimentListToDTOList(findAllExperiments());
    }

    @Override
    public void deleteExperimentsAndExecutionsFromDeletedUser(@NonNull UUID ownerId, @NonNull boolean deleteAll) {
        List<ExperimentDetails> experimentDetails = experimentRepository.findExperimentDetailsByOwnerIdOrPermittedAccountsContaining(ownerId, ownerId);
        for (ExperimentDetails experiment : experimentDetails) {

            if (experiment.getOwnerId() == ownerId) {

                if (deleteAll || experiment.getPermittedAccounts().isEmpty()) { // Deletes all executions from the experiment and the experiment for all users
                    List<ExecutionDetails> executionList = executionRepository.deleteAllByExperimentDetails_Id(experiment.getId());
                    experimentRepository.delete(experiment);
                    // TODO: serviceclient zum userservice fürs komplette nutzerdeleten einfügen
                } else { // Deletes only the executions from the owner
                    List<ExecutionDetails> executionList = executionRepository.deleteAllByExperimentDetails_OwnerId(ownerId);
                }

            } else {
                experiment.getPermittedAccounts().remove(ownerId);
                List<ExecutionDetails> executionList = executionRepository.deleteAllByExperimentDetails_OwnerId(ownerId);
            }

            // TODO: ExecutionList an Kubernetes zum Löschen der Daten schicken

        }
    }

}
