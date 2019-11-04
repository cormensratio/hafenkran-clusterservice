package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.UserDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InitDatabaseImpl implements CommandLineRunner {

    private final UserService userService;

    private final ExperimentService experimentService;

    @Override
    public void run(String... args) throws Exception {

        final UserDetails userDetails1 = new UserDetails("Kurt", "password");
        userService.createUser(userDetails1);

        final ExperimentDetails experimentDetails1 = new ExperimentDetails(UUID.fromString("c8aef4f2-92f8-47eb-bbe9-bd457f91f0e6"), "experiment1", 300);
        experimentService.createExperiment(experimentDetails1);

        final ExperimentDetails experimentDetails2 = new ExperimentDetails(UUID.fromString("c8aef4f2-92f8-47eb-bbe9-bd457f91f0e6"), "experiment2", 1024);
        experimentService.createExperiment(experimentDetails2);

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
