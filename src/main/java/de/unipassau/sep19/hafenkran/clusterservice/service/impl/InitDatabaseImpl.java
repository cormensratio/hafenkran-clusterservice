package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.UserDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class InitDatabaseImpl implements CommandLineRunner {

    @Autowired
    UserService userService;

    @Autowired
    ExperimentService experimentService;

    @Override
    public void run(String... args) throws Exception {

        final UserDetails userDetails1 = new UserDetails("Kurt", "password", Collections.emptyList());
        userService.createUser(userDetails1);

        final ExperimentDetails experimentDetails1 = new ExperimentDetails("experiment1", userDetails1, 300);
        experimentService.createExperiment(experimentDetails1);

        log.info("Database initialized!");
    }
    /*
    @Autowired
    TestServiceImpl testService;

    @Override
    public void run(String... args) throws Exception{

        final Test test1 = new Test();
        testService.createTest(test1);

        log.info("database initialized");
    }
*/
}
