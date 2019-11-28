package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
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
import java.util.stream.Collectors;

/**
 * Provides {@link ExperimentDetails}, {@link ExperimentDTO} and {@link ExperimentDTOList} specific services.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExperimentServiceImpl implements ExperimentService {

    private final ExperimentRepository experimentRepository;

    private List<ExperimentDetails> findExperimentsListOfUserId(@NonNull UUID userId) {
        List<ExperimentDetails> experimentDetailsByUserId = experimentRepository.findExperimentDetailsByOwnerId(userId);
        experimentDetailsByUserId.forEach(ExperimentDetails::validatePermissions);
        return experimentDetailsByUserId;
    }

    /**
     * {@inheritDoc}
     */
    public ExperimentDetails createExperiment(@Valid @NonNull ExperimentDetails experimentDetails) {
        if (!checkIfExperimentNameAlreadyUsedByUser(retrieveExperimentsDTOListOfUserId(experimentDetails.getOwnerId()),
                experimentDetails)) {
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
    public void createExperimentDatabaseInit(@Valid @NonNull ExperimentDetails experimentDetails) {
        final ExperimentDetails savedExperimentDetails = experimentRepository.save(experimentDetails);
        log.info(String.format("Experiment with id %s created", savedExperimentDetails.getId()));
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

    private boolean checkIfExperimentNameAlreadyUsedByUser(@NonNull List<ExperimentDTO> userExperimentList,
                                                           @NonNull ExperimentDetails experimentDetails) {
        List<String> userExperimentNames = userExperimentList
                .stream()
                .map(ExperimentDTO::getName)
                .collect(Collectors.toList());
        return userExperimentNames.contains(experimentDetails.getName());
    }
}
