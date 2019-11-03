package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.model.Test;
import de.unipassau.sep19.hafenkran.clusterservice.repository.TestRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.TestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestServiceImpl implements TestService {

    private final TestRepository testRepository;

    public Test createTest(@Valid Test test) {
        final Test savedTest = testRepository.save(test);

        log.info(String.format("Test with id %s created", savedTest.getId()));

        return savedTest;
    }
}
