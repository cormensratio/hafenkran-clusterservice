package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentUpdateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.UserDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
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

    private List<ExperimentDetails> findExperimentsListOfUserId(@NonNull UUID userId) {
        List<ExperimentDetails> experimentDetailsByUserId =
                experimentRepository.findExperimentDetailsByOwnerIdOrPermittedUsersContaining(userId, userId);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ExperimentDTO updateExperimentAccess(@NonNull ExperimentUpdateDTO experimentUpdateDTO) {
        ExperimentDetails experimentDetails = experimentRepository.findById(experimentUpdateDTO.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ExperimentDetails.class, "experimentId", experimentUpdateDTO.getId().toString()));
        UserDTO currentUser = SecurityContextUtil.getCurrentUserDTO();

        // If the owner is not the current user and if the current user is no admin
        if (!experimentDetails.getOwnerId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not the owner, so you can't add other users to the experiment.");

            // If the owner is the current user or if the current user is an admin
        } else if (experimentDetails.getOwnerId().equals(currentUser.getId()) || currentUser.isAdmin()) {
            if (experimentUpdateDTO.getPermittedUsers().toArray().length == experimentDetails.getPermittedUsers().toArray().length) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "There were no changes.");
            }
            experimentDetails.setPermittedUsers(experimentUpdateDTO.getPermittedUsers());
            experimentRepository.save(experimentDetails);
            return ExperimentDTO.fromExperimentDetails(experimentDetails);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You have no permission to update the experiment.");
        }
    }

}
