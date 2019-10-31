package de.unipassau.sep19.hafenkran.clusterservice.repository;

import de.unipassau.sep19.hafenkran.clusterservice.model.UserDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<UserDetails, UUID> {
    Optional<UserDetails> findByName(String name);
}
