package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceAlreadyExistsException;
import de.unipassau.sep19.hafenkran.clusterservice.model.UserDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.UserRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final ExperimentServiceImpl experimentServiceImpl;

    public UserDetails createUser(@Valid UserDetails userDetails) {
        if (userRepository.findByName(userDetails.getName()).isPresent()) {
            throw new ResourceAlreadyExistsException(UserDetails.class, "name", userDetails.getName());
        }

        userDetails.setPassword(userDetails.getPassword());
        userRepository.save(userDetails);
        return userDetails;
    }

    // TODO: Add real authentication functionality
    public UserDetails getCurrentUserById() {
        // sessionToken.authentication.getUser...
        return null;
    }
}
