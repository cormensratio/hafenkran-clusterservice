package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.model.Test;

import javax.validation.Valid;

public interface TestService {

    public Test createTest(@Valid Test test);

}
