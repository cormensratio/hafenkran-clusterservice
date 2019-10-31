package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceAlreadyExistsException;
import de.unipassau.sep19.hafenkran.clusterservice.model.UserDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.UserRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExperimentServiceImpl experimentServiceImpl;

    public UserDetails createUser(@Valid UserDetails userDetails){
        if (userRepository.findByName(userDetails.getName()).isPresent()){
            throw new ResourceAlreadyExistsException(UserDetails.class, "name", userDetails.getName());
        }

        userDetails.setPassword(userDetails.getPassword());
        userRepository.save(userDetails);
        return userDetails;
    }

    public UserDetails findUserByName(@NotBlank String userName){
        final Optional<UserDetails> user = userRepository.findByName(userName);
        return user.orElse(null);
    }


}
