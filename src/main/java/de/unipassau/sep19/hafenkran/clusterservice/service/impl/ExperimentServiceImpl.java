package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import de.unipassau.sep19.hafenkran.clusterservice.repository.UserRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Slf4j
@Service
public class ExperimentServiceImpl implements ExperimentService {

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private UserRepository userRepository;

    public ExperimentDetails createExperiment(@Valid ExperimentDetails experimentDetails) {
        final ExperimentDetails savedExperimentDetails = experimentRepository.save(experimentDetails);

        log.info(String.format("Experiment with id %s created", savedExperimentDetails.getId()));

        return savedExperimentDetails;
    }

    public ExperimentDetails findExperimentById(@NotNull Long id) {
        final Optional<ExperimentDetails> experimentDetails = experimentRepository.findById(id);

        if (! experimentDetails.isPresent()) {
            throw new ResourceNotFoundException(ExperimentDetails.class, "id",
                    id.toString());
        }

        return experimentDetails.get();
    }

    public ExperimentDTO findExperimentDTOById(@NotNull Long id) {
        final Optional<ExperimentDetails> exper = experimentRepository.findById(id);

        if (! exper.isPresent()) {
            throw new ResourceNotFoundException(ExperimentDetails.class, "id",
                    id.toString());
        }

        return new ExperimentDTO(exper.get());
    }
}
