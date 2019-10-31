package de.unipassau.sep19.hafenkran.clusterservice.repository;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperimentRepository extends CrudRepository<ExperimentDetails, Long> {
}
