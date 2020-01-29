package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.dto.PermittedUsersUpdateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.UserDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExecutionRepository;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.serviceclient.ReportingServiceClient;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import io.kubernetes.client.ApiException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Nonnegative;
import javax.persistence.RollbackException;
import javax.validation.Valid;
import java.util.*;

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

    private final KubernetesClient kubernetesClient;

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
        experimentDetails.validatePermissions();

        if (!experimentRepository.findExperimentDetailsByOwnerIdAndName(experimentDetails.getOwnerId(), experimentDetails.getName()).isEmpty()) {
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
        if (!experimentDetails.getPermittedUsers().contains(currentUser.getId()) || !currentUser.isAdmin()) {
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
        List<ExperimentDetails> experimentDetailsList = experimentRepository.findExperimentDetailsByPermittedUsersContaining(ownerId);
        Set<UUID> executionIdList = new HashSet<>();

        for (ExperimentDetails experimentDetails : experimentDetailsList) {
            executionIdList.addAll(deleteExperimentAndAllExecutions(experimentDetails, ownerId, false));
        }

        // Deletes the results in the ReportingService
        reportingServiceClient.deleteResults(executionIdList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteExperimentById(@NonNull UUID experimentId, @NonNull boolean deleteEverything) {
        ExperimentDetails experimentDetails = experimentRepository.findById(experimentId).orElseThrow(
                () -> new ResourceNotFoundException(ExperimentDetails.class, "experimentId", experimentId.toString()));
        UserDTO currentUser = SecurityContextUtil.getCurrentUserDTO();

        Set<UUID> executionIdList = deleteExperimentAndAllExecutions(experimentDetails, currentUser.getId(), deleteEverything);

        // Deletes the results in the ReportingService
        reportingServiceClient.deleteResults(executionIdList);
    }

    /**
     * Deletes all executions from the experiment and the experiment for all users, also within Kubernetes.
     *
     * @param experimentDetails The experiment to be deleted.
     * @return An Set of ids from the deleted executions.
     */
    private Set<UUID> deleteExperimentAndAllExecutions(@NonNull ExperimentDetails experimentDetails, @NonNull UUID userId, @NonNull boolean deleteEverything) {
        List<ExecutionDetails> executionDetailsList;

        experimentDetails.getPermittedAccounts().remove(userId);

        if (deleteEverything || experimentDetails.getPermittedAccounts().isEmpty()) {
            executionDetailsList = executionRepository.deleteAllByExperimentDetails_Id(experimentDetails.getId());
            experimentRepository.delete(experimentDetails);

            String namespace = experimentDetails.getId().toString();
            // Deletes the namespace from the experiment in Kubernetes
            try {
                kubernetesClient.deleteNamespace(namespace);
            } catch (ApiException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The namespace couldn't be deleted.");
            }
        } else {
            executionDetailsList = executionRepository.deleteAllByOwnerId(userId);
        }

        // Deletes the results in the ReportingService
        Set<UUID> executionIdList = new HashSet<>();
        for (ExecutionDetails executionDetails : executionDetailsList) {
            executionIdList.add(executionDetails.getId());
        }

        return executionIdList;
    }

}
