package de.unipassau.sep19.hafenkran.clusterservice.service;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * The UploadService for uploading and storing files to an experiment.
 */
public interface UploadService {

    /**
     * Stores a file at the location ROOT_PATH/{userId}/{experimentId}.
     *
     * @param file           The {@link MultipartFile} which should be saved.
     * @param experimentName The name for the new experiment.
     * @return An ExperimentDTO with all new experimentDetails.
     */
    ExperimentDTO storeFile(@NonNull MultipartFile file, @NonNull String experimentName);
}
