package de.unipassau.sep19.hafenkran.clusterservice.util;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
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

    @Value("${mockdata}")
    private boolean mockdata;

    @Override
    public void run(String... args) {

        if (!mockdata) {
            return;
        }

        final ExperimentDetails experimentDetails1 = new ExperimentDetails(
                UUID.fromString("00000000-0000-0000-0000-000000000001"), "experiment1", 300);
        experimentService.createExperiment(experimentDetails1);

        final ExperimentDetails experimentDetails2 = new ExperimentDetails(
                UUID.fromString("00000000-0000-0000-0000-000000000001"), "experiment2", 1024);
        experimentService.createExperiment(experimentDetails2);

        log.info("Database initialized!");
    }
}
