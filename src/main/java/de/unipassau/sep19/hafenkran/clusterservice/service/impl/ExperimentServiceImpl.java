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
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExperimentServiceImpl implements ExperimentService {

    private final ExperimentRepository experimentRepository;

    private List<ExperimentDetails> findExperimentsListOfUserId(@NonNull UUID userId) {
        return experimentRepository.findExperimentDetailsByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    public ExperimentDetails createExperiment(@Valid @NonNull ExperimentDetails experimentDetails) {
        final ExperimentDetails savedExperimentDetails = experimentRepository.save(experimentDetails);

        log.info(String.format("Experiment with id %s created", savedExperimentDetails.getId()));

        return savedExperimentDetails;
    }

    /**
     * {@inheritDoc}
     */
    public ExperimentDetails findExperimentById(@NonNull UUID id) {
        final Optional<ExperimentDetails> experimentDetails = experimentRepository.findById(id);

        return experimentDetails.orElseThrow(() -> new ResourceNotFoundException(ExperimentDetails.class, "id",
                id.toString()));
    }

    /**
     * {@inheritDoc}
     */
    public ExperimentDTO findExperimentDTOById(@NonNull UUID id) {
        final Optional<ExperimentDetails> experiment = experimentRepository.findById(id);

        return new ExperimentDTO(experiment.orElseThrow(() -> new ResourceNotFoundException(ExperimentDetails.class, "id",
                id.toString())));
    }

    /**
     * {@inheritDoc}
     */
    public List<ExperimentDTO> findExperimentsDTOListOfUserId(@NonNull UUID userId) {
        return ExperimentDTOList.convertExperimentListToDTOList(findExperimentsListOfUserId(userId));
    }
}
