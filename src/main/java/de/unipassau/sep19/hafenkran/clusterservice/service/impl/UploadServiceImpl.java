package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PushImageResultCallback;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceStorageException;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.service.UploadService;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import io.kubernetes.client.ApiException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

/**
 * The UploadService for uploading and storing files to an experiment.
 */
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Component
@Slf4j
public class UploadServiceImpl implements UploadService {

    private static final int PUSH_TIMEOUT = 130;
    private final ExperimentService experimentService;

    private final KubernetesClient kubernetesClient;

    @Value("${dockerHubRepoPath}")
    private String DOCKER_HUB_REPO_PATH;

    @Value("${experimentsFileUploadLocation}")
    private String path;

    private Path getFileStoragePath(@NonNull ExperimentDetails experimentDetails) {
        return Paths.get(String
                .format("%s/%s/%s", path, experimentDetails.getOwnerId(), experimentDetails.getId()))
                .toAbsolutePath()
                .normalize();
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

            InputStream imageStream = getImageInputStream(experimentDetails);
            pushImageToDockerHub(imageStream, experimentDetails);

            experimentService.createExperiment(experimentDetails);

            try {
                kubernetesClient.createNamespace(experimentDetails);
            } catch (ApiException e) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "There was an error while " +
                        "communicating with the cluster.", e);
            }

            return ExperimentDTO.fromExperimentDetails(experimentDetails);
        } catch (IOException e) {
            throw new ResourceStorageException("Could not store the file '" + fileName + "'. Please try again!", e);
        }
    }

    private Path getPathToTar(@NonNull ExperimentDetails experimentDetails) {
        return Paths.get(getFileStoragePath(experimentDetails) + "/"
                + experimentDetails.getFileName());
    }

    private InputStream getImageInputStream(@NonNull ExperimentDetails experimentDetails) {
        InputStream inputStream;
        try {
            inputStream =
                    Files.newInputStream(getPathToTar(experimentDetails));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Uploaded experiment " + experimentDetails.getName() + " with id " + experimentDetails.getId()
                            + " could not be extracted from file.", ex);
        }
        log.info("Successfully extracted the uploaded file.");
        return inputStream;
    }

    private TarArchiveInputStream getTarInputStream(@NonNull ExperimentDetails experimentDetails) {
        TarArchiveInputStream tarStream;
        try {
            tarStream =
                    new TarArchiveInputStream(new FileInputStream(new File(String.valueOf(getPathToTar(experimentDetails)))));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Uploaded experiment " + experimentDetails.getName() + " with id " + experimentDetails.getId()
                            + " could not be extracted from file.", ex);
        }
        return tarStream;
    }

    private DockerClient initializeDockerClient() {
        DefaultDockerClientConfig.Builder config = DefaultDockerClientConfig
                .createDefaultConfigBuilder();

        DockerClient dockerClient = DockerClientBuilder
                .getInstance(config)
                .build();
        log.debug("Created default docker client");

        return dockerClient;
    }

    private String extractImageIdFromTar(@NonNull ExperimentDetails experimentDetails) {
        TarArchiveInputStream tarStream = getTarInputStream(experimentDetails);
        TarArchiveEntry tarEntry;
        String imageId = "";
        try {
            while ((tarEntry = tarStream.getNextTarEntry()) != null) {
                if (tarEntry.isFile() && tarEntry.getName().equals("manifest.json")) {
                    try {
                        File destFile = new File(String.valueOf(getFileStoragePath(experimentDetails)));
                        File outputFile = new File(destFile + File.separator + tarEntry.getName());
                        outputFile.getParentFile().mkdirs();
                        IOUtils.copy(tarStream, new FileOutputStream(outputFile));
                        String manifestString = Files.readAllLines(Paths.get(outputFile.getPath())).toString();

                        // Builds the substring of of the shortened image id in the manifest.json file.
                        imageId = manifestString.substring(13, 25);
                        outputFile.delete();
                        tarStream.close();
                    } catch (IOException ex) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Image id of uploaded experiment " + experimentDetails.getName() + " with id " + experimentDetails.getId()
                                        + " could not be extracted from file.", ex);
                    }
                    return imageId;
                }
            }
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error reading entry for manifest.json in uploaded experiment " + experimentDetails.getName() +
                            " with id " + experimentDetails.getId(), ex);
        }
        if (imageId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Entry of manifest.json could not be found in " + experimentDetails.getName() + " with id " + experimentDetails.getId());
        }
        return imageId;
    }

    private void pushImageToDockerHub(@NonNull InputStream inputStream,
                                      @NonNull ExperimentDetails experimentDetails) {

        final DockerClient dockerClient = initializeDockerClient();

        String imageId = extractImageIdFromTar(experimentDetails);
        String tag = experimentDetails.getId().toString();

        // Custom configuration for docker account
        /*
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withRegistryEmail("hafenkran@protonmail.com")
                .withRegistryPassword("president encode cold")
                .withRegistryUsername("hafenkran")
                .withDockerCertPath("~/.docker/")
                .withDockerConfig("~/.docker/")
                .withDockerTlsVerify("1")
                .withDockerHost("tcp://localhost:2376").build();

        */

        dockerClient.loadImageCmd(inputStream).exec();
        log.debug("Successfully loaded the image with experiment name " + experimentDetails.getName()
                + " and experiment id " + experimentDetails.getId() + " into the local registry.");

        try {
            inputStream.close();
        } catch (IOException ex) {
            log.debug("An error occurred while closing the input stream.", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        dockerClient.tagImageCmd(imageId, DOCKER_HUB_REPO_PATH, tag).exec();
        log.debug("Tagged the loaded image with experiment id " + experimentDetails.getId());

        try {
            dockerClient.pushImageCmd(DOCKER_HUB_REPO_PATH)
                    .withTag(experimentDetails.getId().toString())
                    .exec(new PushImageResultCallback())
                    .awaitCompletion(PUSH_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        log.debug("Successfully pushed the image to the docker-hub repository: " + DOCKER_HUB_REPO_PATH);
        log.info("Successfully uploaded the image.");

        /*
         The image could be referenced in multiple containers and would need a force remove. The workaround is to
         remove the tagged instance by tag name and afterwards the base image by its image id.
        */
        dockerClient.removeImageCmd(DOCKER_HUB_REPO_PATH + ":" + experimentDetails.getId()).exec();
        dockerClient.removeImageCmd(imageId).exec();
        log.debug("Successfully removed the image from the local registry.");
    }

}
