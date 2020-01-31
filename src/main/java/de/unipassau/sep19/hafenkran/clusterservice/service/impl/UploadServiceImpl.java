package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PushImageResultCallback;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.NodeMetricsDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceStorageException;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.service.MetricsService;
import de.unipassau.sep19.hafenkran.clusterservice.service.UploadService;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import io.kubernetes.client.ApiException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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
    private final ExperimentRepository experimentRepository;
    private final MetricsService metricsService;

    @Value("${experimentsFileUploadLocation}")
    private String path;

    @Value("${dockerHubRepoPath}")
    private String DOCKER_HUB_REPO_PATH;

    @Value("${dockerClient.email}")
    private String dockerEmail;

    @Value("${dockerClient.password}")
    private String dockerPassword;

    @Value("${dockerClient.username}")
    private String dockerUsername;

    @Value("${dockerClient.certPath}")
    private String dockerCertPath;

    @Value("${dockerClient.configPath}")
    private String dockerConfigPath;

    @Value("${dockerClient.tls}")
    private String dockerTlsVerify;

    @Value("${dockerClient.host}")
    private String dockerHost;

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

        if (StringUtils.isEmpty(fileName)) {
            throw new ResourceStorageException("Filename is empty.");
        } else if (fileName.contains("..")) {
            throw new ResourceStorageException("Filename contains invalid path sequence " + fileName);
        }

        // Copy file into the target location
        Path uploadLocation = fileStorageLocation.resolve(fileName);

        try {
            Files.copy(file.getInputStream(), uploadLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ResourceStorageException("Could not store the file '" + fileName + "'. Please try again!", ex);
        }

        try (InputStream imageStream = getImageInputStream(experimentDetails)) {
            pushImageToDockerHub(imageStream, experimentDetails);
        } catch (IOException e) {
            log.debug("An error occurred while closing the input stream.", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ArrayList<NodeMetricsDTO> nodeMetricsList = metricsService.retrieveNodeMetrics();
        try {
            boolean clusterHasEnoughFreeCapacity = false;

            for (NodeMetricsDTO nodeMetric : nodeMetricsList) {
                if (kubernetesClient.checkIfEnoughNodeCapacityFree(nodeMetric.getMetadata().getName(),
                        Integer.parseInt(nodeMetric.getUsage().getCpu()),
                        Integer.parseInt(nodeMetric.getUsage().getMemory()))) {
                    clusterHasEnoughFreeCapacity = true;
                    kubernetesClient.createNamespace(experimentDetails);
                    experimentService.createExperiment(experimentDetails);
                    break;
                }
            }

            if (!clusterHasEnoughFreeCapacity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The cluster is at his max capacity. Please try later.");
            }
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "There was an error while "
                    + "communicating with the cluster.");
        }

        return ExperimentDTO.fromExperimentDetails(experimentDetails);
    }


    private Path getPathToTar(@NonNull ExperimentDetails experimentDetails) {
        return Paths.get(getFileStoragePath(experimentDetails) + "/"
                + experimentDetails.getFileName());
    }

    private InputStream getImageInputStream(@NonNull ExperimentDetails experimentDetails) {
        try {
            InputStream inputStream = Files.newInputStream(getPathToTar(experimentDetails));
            log.info("Successfully extracted the uploaded file.");
            return inputStream;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Uploaded experiment " + experimentDetails.getName() + " with id " + experimentDetails.getId()
                            + " could not be extracted from file.", ex);
        }
    }

    private TarArchiveInputStream getTarInputStream(@NonNull ExperimentDetails experimentDetails) {
        try {
            return new TarArchiveInputStream(new FileInputStream(new File(String.valueOf(getPathToTar(experimentDetails)))));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Uploaded experiment " + experimentDetails.getName() + " with id " + experimentDetails.getId()
                            + " could not be extracted from file.", ex);
        }
    }

    private DockerClient initializeDockerClient() {

        // Custom configuration for docker client
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withRegistryEmail(dockerEmail)
                .withRegistryPassword(dockerPassword)
                .withRegistryUsername(dockerUsername)
                .withDockerCertPath(dockerCertPath)
                .withDockerConfig(dockerConfigPath)
                .withDockerTlsVerify(dockerTlsVerify)
                .withDockerHost(dockerHost).build();

        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
        log.debug("Created docker client");

        return dockerClient;
    }

    private String extractImageIdFromTar(@NonNull ExperimentDetails experimentDetails) {
        TarArchiveEntry tarEntry;
        String imageId = "";
        try (TarArchiveInputStream tarStream = getTarInputStream(experimentDetails)) {
            while ((tarEntry = tarStream.getNextTarEntry()) != null) {
                if (tarEntry.isFile() && tarEntry.getName().equals("manifest.json")) {
                    File destFile = new File(String.valueOf(getFileStoragePath(experimentDetails)));
                    File outputFile = new File(destFile + File.separator + tarEntry.getName());
                    outputFile.getParentFile().mkdirs();
                    IOUtils.copy(tarStream, new FileOutputStream(outputFile));
                    String manifestString = Files.readAllLines(Paths.get(outputFile.getPath())).toString();
                    imageId = manifestString.substring(13, 25);
                    outputFile.delete();
                    break;
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
        experimentDetails.validatePermissions();

        final DockerClient dockerClient = initializeDockerClient();
        final String imageId = extractImageIdFromTar(experimentDetails);
        final byte[] imageByteArray = convertInputStreamToByteArray(inputStream);

        String checksum = calculateChecksum(imageByteArray);
        log.debug("Calculated checksum of image with id: " + imageId + " and checksum " + checksum);

        experimentDetails.setChecksum(checksum);

        if (experimentRepository.findFirstByChecksum(checksum).isPresent()) {
            log.debug("Image with checksum " + checksum + " already exists and upload is skipped.");
            return;
        }

        dockerClient.loadImageCmd(new ByteArrayInputStream(imageByteArray)).exec();
        log.debug("Successfully loaded the image with experiment name " + experimentDetails.getName() + " into the local registry.");

        dockerClient.tagImageCmd(imageId, DOCKER_HUB_REPO_PATH, checksum).exec();
        log.debug("Tagged the loaded image with its checksum " + checksum);

        try {
            dockerClient.pushImageCmd(DOCKER_HUB_REPO_PATH)
                    .withTag(checksum)
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
        try {
            dockerClient.removeImageCmd(DOCKER_HUB_REPO_PATH + ":" + checksum).exec();
            dockerClient.removeImageCmd(imageId).exec();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }


        log.debug("Successfully removed the image from the local registry.");
    }

    private byte[] convertInputStreamToByteArray(@NonNull InputStream inputStream) {
        byte[] imageByteArray;
        try {
            imageByteArray = IOUtils.toByteArray(inputStream);
        } catch (IOException ex) {
            log.debug("An error occurred while reading the image input stream to the byte array");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return imageByteArray;
    }

    private String calculateChecksum(@NonNull byte[] imageByteArray) {
        return DigestUtils.md5Hex(imageByteArray);
    }
}
