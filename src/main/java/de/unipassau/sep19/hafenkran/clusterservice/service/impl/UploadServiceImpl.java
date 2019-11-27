package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceStorageException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.service.UploadService;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


/**
 * The UploadService for uploading and storing files to an experiment.
 */
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Component
@Slf4j
public class UploadServiceImpl implements UploadService {

    private final ExperimentService experimentService;

    @Value("${experimentsFileUploadLocation}")
    private String path;

    private Path getFileStoragePath(@NonNull ExperimentDetails experimentDetails) {
        return Paths.get(
                String.format("%s/%s/%s", path, experimentDetails.getOwnerId(), experimentDetails.getId()))
                .toAbsolutePath().normalize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExperimentDTO storeFile(@NonNull MultipartFile file, @NonNull String experimentName) {

        String fileName = file.getOriginalFilename();

        ExperimentDetails experimentDetails = new ExperimentDetails(SecurityContextUtil.getCurrentUserDTO().getId(),
                experimentName, fileName, file.getSize());

        if (StringUtils.isEmpty(path)) {
            throw new ResourceStorageException("The experimentsFileUploadPath is not configured correctly");
        }

        // Configure exact naming of fileStorageLocation-path
        Path fileStorageLocation = getFileStoragePath(experimentDetails);

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

            loadImageFromTar()

            experimentService.createExperiment(experimentDetails);

            return ExperimentDTO.fromExperimentDetails(experimentDetails);
        } catch (IOException e) {
            throw new ResourceStorageException("Could not store the file '" + fileName + "'. Please try again!", e);
        }
    }

    private InputStream loadImageFromTar(@NonNull ExperimentDetails experimentDetails) {

        Path fileStoragePath = getFileStoragePath(experimentDetails);

        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(fileStoragePath);
        } catch (IOException ex) {
            log.info("The image could not be correctly extracted " +
                    "from" + fileStoragePath.toString(), ex);
        }
        return inputStream;
    }

/*
    private void pushImageToRegistry(@NonNull InputStream inputStream,
                                     @NonNull ExecutionDetails executionDetails) throws IOException {

        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withRegistryEmail("hafenkran@protonmail.com")
                .withRegistryPassword("president encode cold")
                .withRegistryUsername("hafenkran")
                .withDockerCertPath("~/.docker/config.json/certs")
                .withDockerConfig("~/.docker/")
                .withDockerTlsVerify("1")
                .withDockerHost("tcp://docker.hafenkran.com:2376").build();

        DockerClient dockerClient =
                DockerClientBuilder.getInstance(config).build();

        // load image in local docker store
        dockerClient.loadImageCmd(inputStream).exec();

            String md5 =
                    org.apache.commons.codec.digest.DigestUtils.md5Hex(image);

        String repository =
                "hafenkran/" + executionDetails.getExperimentDetails().getName();

        String tag = "git";

        dockerClient.tagImageCmd(imageId, repository, tag).exec();

        dockerClient.pushImageCmd("hafenkran")

    }

 */




}
