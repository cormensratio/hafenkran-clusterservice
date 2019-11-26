package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.service.ReportingService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.UUID;

@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Component
public class ReportingServiceImpl implements ReportingService {

    public File getPersistentResults(@NonNull UUID executionId) {
        return new File("home/EPBesprechung.odt");
    }

}