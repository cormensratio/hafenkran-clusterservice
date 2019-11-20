package de.unipassau.sep19.hafenkran.clusterservice.repository;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExperimentRepository extends CrudRepository<ExperimentDetails, UUID> {

    List<ExperimentDetails> findExperimentDetailsByUserId(UUID userId);
}
