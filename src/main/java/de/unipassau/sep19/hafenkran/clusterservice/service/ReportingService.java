package de.unipassau.sep19.hafenkran.clusterservice.service;

import lombok.NonNull;

import java.awt.*;
import java.io.File;
import java.util.UUID;

public interface ReportingService {

    TextField getResults(@NonNull UUID executionId);

    File getResultDocument(@NonNull UUID executionId);

}