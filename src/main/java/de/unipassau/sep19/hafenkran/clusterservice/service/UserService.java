package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.model.UserDetails;

import javax.validation.Valid;

public interface UserService {

    public UserDetails createUser(@Valid UserDetails userDetails);

    public UserDetails getCurrentUserById();

}
