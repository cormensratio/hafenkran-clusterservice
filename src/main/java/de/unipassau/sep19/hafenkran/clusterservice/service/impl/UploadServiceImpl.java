package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PushImageResultCallback;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceStorageException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.service.UploadService;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The UploadService for uploading and storing files to an experiment.
 */
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Component
@Slf4j
public class UploadServiceImpl implements UploadService {

    public static final String HAFENKRAN_HAFENKRAN_REPO = "hafenkran" +
            "/hafenkran" +
            "-repo";
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

            InputStream imageStream = loadImageFromTar(experimentDetails);
            pushImageToDockerHub(imageStream, experimentDetails);

            experimentService.createExperiment(experimentDetails);

            return ExperimentDTO.fromExperimentDetails(experimentDetails);
        } catch (IOException e) {
            throw new ResourceStorageException("Could not store the file '" + fileName + "'. Please try again!", e);
        }
    }

    private InputStream loadImageFromTar(@NonNull ExperimentDetails experimentDetails) {

        Path pathToFile = Paths.get(getFileStoragePath(experimentDetails) + "/"
                + experimentDetails.getFileName());

        //todo: hardcoded path - delete me
        pathToFile = Paths.get("/home/cormensratio/archive.tar");

        InputStream inputStream;
        try {
            inputStream =
                    Files.newInputStream(pathToFile);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Uploaded image could not be extracted from file.", ex);
        }
        return inputStream;
    }

    private void pushImageToDockerHub(@NonNull InputStream inputStream,
                                      @NonNull ExperimentDetails experimentDetails) {

        // Configuration for docker account
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

        DefaultDockerClientConfig.Builder config
                = DefaultDockerClientConfig.createDefaultConfigBuilder();
        DockerClient dockerClient = DockerClientBuilder
                .getInstance(config)
                .build();

        dockerClient.createImageCmd(
                HAFENKRAN_HAFENKRAN_REPO + ":" +
                        experimentDetails.getId(), inputStream).exec();

        try {
            dockerClient.pushImageCmd(HAFENKRAN_HAFENKRAN_REPO)
                    .withTag(experimentDetails.getId().toString()).exec(new PushImageResultCallback())
                    .awaitCompletion(130, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        List<Image> images =
                dockerClient.listImagesCmd().withImageNameFilter(HAFENKRAN_HAFENKRAN_REPO + ":" + experimentDetails.getId()).exec();

        Image image = images.get(0);

        System.out.println("HERE " + image.getId().substring(7, 19));

        dockerClient.removeImageCmd(image.getId().substring(7, 19)).exec();

        //logging
        //refactor
        //get(0)
    }
}
