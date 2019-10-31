package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.model.Test;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitDatabaseImpl implements CommandLineRunner {


    @Autowired
    TestServiceImpl testService;

    @Override
    public void run(String... args) throws Exception{

        final Test test1 = new Test();
        testService.createTest(test1);

        log.info("database initialized");
    }




/*
    @Autowired
    UserService userService;

    @Autowired
    ExperimentService experimentService;

    @Override
    public void run(String... args) throws Exception {

        final UserDetails userDetails1 = new UserDetails("Kurt", "password", Collections.emptyList());
        userService.createUser(userDetails1);

        final ExperimentDetails experimentDetails1 = new ExperimentDetails("mongo-db", userDetails1, 300);
        experimentService.createExperiment(experimentDetails1);

        log.info("Database initialized!");
    }

*/
}
