package de.unipassau.sep19.hafenkran.clusterservice.util;

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
public class InitDatabase implements CommandLineRunner {

    private final ExperimentService experimentService;

    private final ExecutionService executionService;

    @Value("${mockdata}")
    private boolean mockdata;

    @Override
    public void run(String... args) {

        if (!mockdata) {
            return;
        }

        final ExperimentDetails experimentDetails1 =
                new ExperimentDetails(UUID.fromString(
                        "00000000-0000-0000-0000-000000000001"),
                        "ColdFusionAlgorithm",
                        "ColdFusionAlgorithm", 300);
        experimentDetails1.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        experimentDetails1.getPermittedUsers().add(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        experimentService.createExperiment(experimentDetails1);

        final ExperimentDetails experimentDetails2 =
                new ExperimentDetails(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                        "CompletePI",
                        "CompletePI", 1024);
        experimentDetails2.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        experimentDetails2.getPermittedUsers().add(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        experimentService.createExperiment(experimentDetails2);

        final ExperimentDetails experimentDetails3 =
                new ExperimentDetails(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                        "CompletePI",
                        "CompletePI", 1024);
        experimentDetails3.setId(UUID.fromString("00000000-0000-0000-0000-000000000003"));
        experimentDetails3.getPermittedUsers().add(UUID.fromString("00000000-0000-0000-0000-000000000003"));
        experimentService.createExperiment(experimentDetails3);

        final ExecutionDetails executionDetails = new ExecutionDetails(
                UUID.fromString("00000000-0000-0000-0000-000000000001"), experimentDetails1,
                experimentDetails1.getName(), 100, 10, 60);
        executionDetails.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        executionService.createExecution(executionDetails);

        log.info("Database initialized!");
    }
}
