package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InitDatabaseImpl implements CommandLineRunner {

    private final ExperimentService experimentService;

    private final ExecutionService executionService;

    @Value("${mockdata}")
    private boolean mockdata;

    @Override
    public void run(String... args) throws Exception {

        if (! mockdata) {
            return;
        }

        final ExperimentDetails experimentDetails1 = new ExperimentDetails(UUID.fromString("c8aef4f2-92f8-47eb-bbe9-bd457f91f0e6"), "experiment1", 300);
        experimentService.createExperiment(experimentDetails1);

        final ExperimentDetails experimentDetails2 = new ExperimentDetails(UUID.fromString("c8aef4f2-92f8-47eb-bbe9-bd457f91f0e6"), "experiment2", 1024);
        experimentService.createExperiment(experimentDetails2);

        final ExecutionDetails executionDetails1 =
                new ExecutionDetails(experimentDetails1.getId(), "execution1");
        executionService.createExecution(executionDetails1);

        log.info("Database initialized!");
    }
}
