package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    String storeFile(MultipartFile file, ExperimentDetails experimentDetails);
}
