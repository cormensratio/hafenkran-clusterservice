package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceStorageException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.UploadService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * The UploadService for uploading and storing files to an experiment.
 */
@RequiredArgsConstructor
@Component
public class UploadServiceImpl implements UploadService {

    @Value("${experimentsFileUploadLocation}")
    private String path;

    /**
     * Stores a file within the {@code fileStorageLocation} using {@link Path#resolve(String fileName)}. The path will
     * contain {@code path}/{userId}/{experimentId}.
     *
     * @param file              The {@link MultipartFile} which should be saved.
     * @param experimentDetails The details from the new experiment.
     * @return An ExperimentDTO with all new experimentDetails.
     */
    @Override
    public ExperimentDTO storeFile(@NonNull MultipartFile file, @Valid @NonNull ExperimentDetails experimentDetails) {
        String fileName = file.getOriginalFilename();

        if (StringUtils.isEmpty(path)) {
            throw new ResourceStorageException("The experimentsFileUploadPath is not configured correctly");
        }

        // Configure exact naming of fileStorageLocation-path
        Path fileStorageLocation = Paths.get(String.format("%s/%s/%s", path, experimentDetails.getOwnerId(), experimentDetails.getId()))
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception e) {
            throw new ResourceStorageException("Could not create the directory where the uploaded files will be stored.", e);
        }

        try {
            if (StringUtils.isEmpty(fileName)) {
                throw new ResourceStorageException("Filename is empty.");
            } else if (fileName.contains("..")) {
                throw new ResourceStorageException("Filename contains invalid path sequence " + fileName);
            }

            // Copy file into the target location
            Path uploadLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), uploadLocation, StandardCopyOption.REPLACE_EXISTING);

            return new ExperimentDTO(experimentDetails);
        } catch (IOException e) {
            throw new ResourceStorageException("Could not store the file '" + fileName + "'. Please try again!", e);
        }
    }

}
