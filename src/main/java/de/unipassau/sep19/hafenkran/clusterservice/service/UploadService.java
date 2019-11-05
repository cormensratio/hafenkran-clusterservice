package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    /**
     * Stores a file at the location ROOT_PATH/{userId}/{experimentId}
     *
     * @param file              the {@link MultipartFile} which should be saved
     * @param experimentDetails the details for the new experiment
     * @return the status of the store operation
     */
    ExperimentDTO storeFile(@NonNull MultipartFile file, @NonNull ExperimentDetails experimentDetails);
}
