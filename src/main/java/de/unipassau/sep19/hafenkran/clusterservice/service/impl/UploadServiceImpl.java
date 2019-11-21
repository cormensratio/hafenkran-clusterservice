package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceStorageException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.service.UploadService;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * The UploadService for uploading and storing files to an experiment.
 */
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Component
public class UploadServiceImpl implements UploadService {

    private final ExperimentService experimentService;

    @Value("${experimentsFileUploadLocation}")
    private String path;

    /**
     * {@inheritDoc}
     */
    @Override
    public ExperimentDTO storeFile(@NonNull MultipartFile file, @NonNull String experimentName) {

        ExperimentDetails experimentDetails = new ExperimentDetails(SecurityContextUtil.getCurrentUserDTO().getId(),
                experimentName, file.getSize());

        experimentService.createExperiment(experimentDetails);

        String fileName = file.getOriginalFilename();

        if (StringUtils.isEmpty(path)) {
            throw new ResourceStorageException("The experimentsFileUploadPath is not configured correctly");
        }

        // Configure exact naming of fileStorageLocation-path
        Path fileStorageLocation = Paths.get(
                String.format("%s/%s/%s", path, experimentDetails.getOwnerId(), experimentDetails.getId()))
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

            return ExperimentDTO.fromExperimentDetails(experimentDetails);
        } catch (IOException e) {
            throw new ResourceStorageException("Could not store the file '" + fileName + "'. Please try again!", e);
        }
    }

}
