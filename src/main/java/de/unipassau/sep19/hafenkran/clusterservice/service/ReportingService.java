package de.unipassau.sep19.hafenkran.clusterservice.service;

import lombok.NonNull;

import java.io.File;
import java.util.UUID;

public interface ReportingService {

    File getPersistentResults(@NonNull UUID executionId);

}