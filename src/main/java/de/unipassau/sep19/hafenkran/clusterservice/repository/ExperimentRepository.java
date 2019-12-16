package de.unipassau.sep19.hafenkran.clusterservice.repository;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExperimentRepository extends CrudRepository<ExperimentDetails, UUID> {

    List<ExperimentDetails> findExperimentDetailsByOwnerId(@NonNull UUID ownerId);

    List<ExperimentDetails> findExperimentDetailsByOwnerIdAndName(@NonNull UUID ownerId, @NonNull String name);

    @org.springframework.lang.NonNull
    List<ExperimentDetails> findAll();
}
