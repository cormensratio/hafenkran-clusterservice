package de.unipassau.sep19.hafenkran.clusterservice.util;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ResultDTO;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.serviceclient.impl.ReportingServiceClientImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InitDatabase implements CommandLineRunner {

    private final ExperimentService experimentService;

    private final ExecutionService executionService;

    private final ReportingServiceClientImpl reportingServiceClient;

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
        experimentService.createExperiment(experimentDetails1);

        final ExperimentDetails experimentDetails2 =
                new ExperimentDetails(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                        "CompletePI",
                        "CompletePI", 1024);
        experimentDetails2.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        experimentService.createExperiment(experimentDetails2);

        final ExecutionDetails executionDetails = new ExecutionDetails(
                UUID.fromString("00000000-0000-0000-0000-000000000001"), experimentDetails1,
                experimentDetails1.getName(), 100, 10, 60);
        executionDetails.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        executionService.createExecution(executionDetails);

        log.info("Database initialized!");
    }
}
