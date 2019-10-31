package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.model.UserDetails;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public interface UserService {

    public UserDetails createUser(@Valid UserDetails userDetails);

    public UserDetails findUserByName(@NotBlank String userName);

}
