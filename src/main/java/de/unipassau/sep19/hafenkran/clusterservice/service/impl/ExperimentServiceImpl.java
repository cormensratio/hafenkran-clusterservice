package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.dto.PermittedUsersUpdateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.UserDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.Resource;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExecutionRepository;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.serviceclient.ReportingServiceClient;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides {@link ExperimentDetails}, {@link ExperimentDTO} and {@link ExperimentDTOList} specific services.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExperimentServiceImpl implements ExperimentService {

    private final ExperimentRepository experimentRepository;

    private final ExecutionRepository executionRepository;

    private final ReportingServiceClient reportingServiceClient;

    private List<ExperimentDetails> findExperimentsListOfUserId(@NonNull UUID userId) {
        List<ExperimentDetails> experimentDetailsByUserId =
                experimentRepository.findExperimentDetailsByPermittedUsersContaining(userId);
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
        if (!experimentRepository.findExperimentDetailsByOwnerIdAndName(experimentDetails.getOwnerId(),
                experimentDetails.getName()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Experimentname: "
                    + experimentDetails.getName() + " already used. Must be unique.");
        }
        ExperimentDetails savedExperimentDetails = experimentRepository.save(experimentDetails);
        log.info(String.format("Experiment with id %s created", experimentDetails.getId()));
        return savedExperimentDetails;
    }

    /**
     * {@inheritDoc}
     */
    public ExperimentDTO retrieveExperimentDTOById(@NonNull UUID id) {
        final Optional<ExperimentDetails> experimentDetailsOptional = experimentRepository.findById(id);
        ExperimentDetails experimentDetails = experimentDetailsOptional.orElseThrow(
                () -> new ResourceNotFoundException(ExperimentDetails.class, "id", id.toString()));

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
    public ExperimentDTO updatePermittedUsers(@NonNull UUID experimentId, @NonNull PermittedUsersUpdateDTO permittedUsersUpdateDTO) {
        if (permittedUsersUpdateDTO.getPermittedUsers().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You cannot forbid everyone the access. There must be at least one person.");
        }

        ExperimentDetails experimentDetails = experimentRepository.findById(experimentId).orElseThrow(
                () -> new ResourceNotFoundException(ExperimentDetails.class, "experimentId", experimentId.toString()));
        UserDTO currentUser = SecurityContextUtil.getCurrentUserDTO();

        // If the current user is not permitted or if the current user is no admin
        if (!experimentDetails.getPermittedUsers().contains(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You are not allowed to change the user access from the current experiment.");
        }

        experimentDetails.setPermittedUsers(permittedUsersUpdateDTO.getPermittedUsers());
        experimentRepository.save(experimentDetails);
        return ExperimentDTO.fromExperimentDetails(experimentDetails);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteExperimentsByOwnerId(@NonNull UUID ownerId) {
        List<ExperimentDetails> experimentDetailsList = experimentRepository.findExperimentDetailsByPermittedUsersContaining(
                ownerId);

        for (ExperimentDetails experimentDetails : experimentDetailsList) {
            deleteExperimentAndAllExecutions(experimentDetails, ownerId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteExperimentById(@NonNull UUID experimentId) {
        ExperimentDetails experimentDetails = experimentRepository.findById(experimentId).orElseThrow(
                () -> new ResourceNotFoundException(ExperimentDetails.class, "experimentId", experimentId.toString()));
        experimentDetails.validatePermissions();
        UserDTO currentUser = SecurityContextUtil.getCurrentUserDTO();
        deleteExperimentAndAllExecutions(experimentDetails, currentUser.getId());
    }

    private void deleteExperimentAndAllExecutions(@NonNull ExperimentDetails experimentDetails, @NonNull UUID userId) {
        /*
        if (experimentDetails.getOwnerId().equals(userId)) {
            Set<UUID> executionIds = executionRepository.deleteByExperimentDetails_Id(
                    experimentDetails.getId()).stream().map(Resource::getId).collect(Collectors.toSet());
            experimentRepository.delete(experimentDetails);
            reportingServiceClient.deleteResults(executionIds);
        } else {
            experimentDetails.getPermittedAccounts().remove(userId);
            experimentRepository.save(experimentDetails);
            Set<UUID> executionIds = executionRepository.deleteByOwnerIdAndExperimentDetails_Id(userId,
                    experimentDetails.getId()).stream().map(Resource::getId).collect(Collectors.toSet());
            reportingServiceClient.deleteResults(executionIds);
        }
        */
        experimentRepository.deleteById(experimentDetails.getId());

    }

}
