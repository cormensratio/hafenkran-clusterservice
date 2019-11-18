package de.unipassau.sep19.hafenkran.clusterservice.repository;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionRepository extends CrudRepository<ExecutionDetails, UUID> {

    List<ExecutionDetails> findAllByExperimentDetails_Id(@NonNull UUID experimentId);

    List<ExecutionDetails> findAllByExperimentDetails_UserId(@NonNull UUID userId);

}
