package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import de.unipassau.sep19.hafenkran.clusterservice.repository.UserRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExperimentServiceImpl implements ExperimentService {

    private final ExperimentRepository experimentRepository;

    private final UserRepository userRepository;

    public ExperimentDetails createExperiment(@Valid ExperimentDetails experimentDetails) {
        final ExperimentDetails savedExperimentDetails = experimentRepository.save(experimentDetails);

        log.info(String.format("Experiment with id %s created", savedExperimentDetails.getId()));

        return savedExperimentDetails;
    }

    public ExperimentDetails findExperimentById(@NotNull UUID id) {
        final Optional<ExperimentDetails> experimentDetails = experimentRepository.findById(id);

        if (!experimentDetails.isPresent()) {
            throw new ResourceNotFoundException(ExperimentDetails.class, "id",
                    id.toString());
        }

        return experimentDetails.get();
    }

    public ExperimentDTO findExperimentDTOById(@NotNull UUID id) {
        final Optional<ExperimentDetails> experiment = experimentRepository.findById(id);

        if (!experiment.isPresent()) {
            throw new ResourceNotFoundException(ExperimentDetails.class, "id",
                    id.toString());
        }

        return new ExperimentDTO(experiment.get());
    }

    // TODO: impl + interface
    public List<ExperimentDetails> getExperimentsListOfUserId(@NotNull UUID id) {
        return null;
    }

    // TODO: impl + interface
    public List<ExperimentDTO> getExperimentsDTOListOfUserId(@NotNull UUID id) {
        return null;
    }
}
