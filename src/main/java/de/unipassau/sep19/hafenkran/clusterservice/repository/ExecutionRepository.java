package de.unipassau.sep19.hafenkran.clusterservice.repository;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionRepository extends CrudRepository<ExecutionDetails, UUID> {

    List<ExecutionDetails> findAllByExperimentDetails_Id(@NonNull UUID experimentId);

    List<ExecutionDetails> findAllByOwnerId(@NonNull UUID ownerId);

    List<ExecutionDetails> findAllByStatus(@NonNull ExecutionDetails.Status status);

    @org.springframework.lang.NonNull
    List<ExecutionDetails> findAll();

    void deleteById(@NonNull UUID executionId);

    ExecutionDetails findByPodNameAndExperimentDetails(@NonNull String podName, @NonNull ExperimentDetails experimentDetails);

    List<ExecutionDetails> deleteByExperimentDetails_Id(@NonNull UUID experimentId);

    List<ExecutionDetails> deleteByOwnerIdAndExperimentDetails_Id(@NonNull UUID ownerId, @NonNull UUID experimentId);

}
