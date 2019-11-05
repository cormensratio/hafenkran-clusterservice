package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceStorageException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.UploadService;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class UploadServiceImpl implements UploadService {

    private String path = "/home/ash265/";

    @Override
    public String storeFile(@NonNull MultipartFile file, @NonNull ExperimentDetails experimentDetails) {
        String fileName = file.getOriginalFilename();
        Path fileStorageLocation = Paths.get(String.format("%s/%s/%s", path, experimentDetails.getUserId(), experimentDetails.getId()))
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception e) {
            throw new ResourceStorageException("Could not create the directory where the uploaded files will be stored.", e);
        }

        try {
            if (fileName.contains("..")) {
                throw new ResourceStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location
            Path uploadLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), uploadLocation, StandardCopyOption.REPLACE_EXISTING); // gleiche namen erlauben aber unterschiedliche ids?

            return fileName;
        } catch (IOException e) {
            throw new ResourceStorageException("Could not store the file '" + fileName + "'. Please try again!", e);
        }
    }


}
