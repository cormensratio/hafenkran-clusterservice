package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTOList;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExecutionRepository;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import de.unipassau.sep19.hafenkran.clusterservice.util.SecurityContextUtil;
import io.kubernetes.client.ApiException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExecutionServiceImpl implements ExecutionService {

    private final ExecutionRepository executionRepository;

    private final ExperimentRepository experimentRepository;

    private final KubernetesClient kubernetesClient;

    @Value("${kubernetes.deployment.defaults.ram}")
    private long ramDefault;

    @Value("${kubernetes.deployment.defaults.cpu}")
    private long cpuDefault;

    @Value("${kubernetes.deployment.defaults.bookedTime}")
    private long bookedTimeDefault;

    /**
     * {@inheritDoc}
     */
    @Override
    public String retrieveLogsForExecutionId(@NonNull UUID id, int lines, Integer sinceSeconds, boolean withTimestamps) {
        final String logs;
        try {
            logs = kubernetesClient.retrieveLogs(retrieveExecutionDetailsById(id), lines, sinceSeconds, withTimestamps);
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "There was an error while " +
                    "communicating with the cluster.", e);
        }
        return logs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionDTO createAndStartExecution(@NonNull ExecutionCreateDTO executionCreateDTO) {
        final ExecutionDetails executionDetails =
                createExecutionFromExecCreateDTO(executionCreateDTO);

        final ExecutionDetails createdExecutionDetails = createExecution(executionDetails);

        final ExecutionDetails startedExecutionDetails = startExecution(createdExecutionDetails);

        return ExecutionDTO.fromExecutionDetails(startedExecutionDetails);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionDetails createExecution(@NonNull ExecutionDetails executionDetails) {
        final ExecutionDetails savedExecutionDetails =
                executionRepository.save(executionDetails);

        log.info(String.format("Execution with id %s created",
                savedExecutionDetails.getId()));

        return savedExecutionDetails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionDTO terminateExecution(@NonNull UUID executionId) {

        ExecutionDetails executionDetails = getExecutionDetails(executionId);

        String podName = "";

        try {
            kubernetesClient.deletePod(executionDetails.getExperimentDetails().getId(),
                    executionDetails.getName());
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "There was an error while " +
                    "communicating with the cluster.", e);
        }

        executionDetails.setPodName(podName);
        executionDetails.setStatus(ExecutionDetails.Status.CANCELED);
        executionDetails.setTerminatedAt(LocalDateTime.now());

        executionRepository.save(executionDetails);

        ExecutionDTO terminatedExecutionDTO = ExecutionDTO.fromExecutionDetails(executionDetails);

        log.info(String.format("Execution with id %S terminated", executionId));

        return terminatedExecutionDTO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionDTO retrieveExecutionDTOById(@NonNull UUID id) {
        return ExecutionDTO.fromExecutionDetails(retrieveExecutionDetailsById(id));
    }

    private ExecutionDetails retrieveExecutionDetailsById(@NonNull UUID id) {
        final Optional<ExecutionDetails> execution = executionRepository.findById(id);
        ExecutionDetails executionDetails = execution.orElseThrow(
                () -> new ResourceNotFoundException(ExecutionDetails.class, "id",
                        id.toString()));
        executionDetails.validatePermissions();
        return executionDetails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ExecutionDTO> retrieveExecutionsDTOListOfExperimentId(@NonNull UUID experimentId) {
        List<ExecutionDetails> executionDetailsList = executionRepository.findAllByExperimentDetails_Id(experimentId);

        if (executionDetailsList.isEmpty()) {
            return Collections.emptyList();
        }

        executionDetailsList.forEach(ExecutionDetails::validatePermissions);
        return ExecutionDTOList.fromExecutionDetailsList(executionDetailsList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ExecutionDTO> retrieveExecutionsDTOListForUserId(@NonNull UUID userId) {
        List<ExecutionDetails> executionDetailsList = executionRepository.findAllByExperimentDetails_OwnerId(userId);

        if (executionDetailsList.isEmpty()) {
            return Collections.emptyList();
        }

        executionDetailsList.forEach(ExecutionDetails::validatePermissions);
        return ExecutionDTOList.fromExecutionDetailsList(executionDetailsList);
    }

    private static LinkedList<String> getAllFiles(File rootFile) {
        File[] files = rootFile.listFiles();
        LinkedList<String> allFiles = new LinkedList<>();

        if (files != null) {
            for (File file : files) {

                if (file.isDirectory()) {
                    LinkedList<String> moreFiles = getAllFiles(file);
                    allFiles.addAll(moreFiles);
                } else {
                    allFiles.add(file.getAbsolutePath());
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "There were no files within the rootFile.");
        }
        return allFiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getResults(@NonNull UUID executionId) {
        ExecutionDetails executionDetails = retrieveExecutionDetailsById(executionId);
        Path resultStoragePath;

        try {
            resultStoragePath = kubernetesClient.retrieveResults(executionDetails);
        } catch (IOException | ApiException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "There was an error while " +
                    "communicating with the cluster.", e);
        }

        byte[] bytes = new byte[1024];

        // Zip results
        try {
            LinkedList<String> fileList = getAllFiles(resultStoragePath.toFile());

            String outFilename = resultStoragePath.toString() + "/results.tar.gz";
            FileOutputStream fos = new FileOutputStream(outFilename);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (String fileName : fileList) {

                FileInputStream fis = new FileInputStream(fileName);
                zos.putNextEntry(new ZipEntry(fileName));

                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }

                zos.closeEntry();
                fis.close();
            }

            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get zipped results
        InputStream in = getClass().getResourceAsStream(resultStoragePath.toString() + "/results.tar.gz");
        byte[] results;
        try {
            results = IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There were no results found at the " +
                    "resultStorageLocation.", e);
        }

        return results;
    }

    private ExecutionDetails startExecution(@NonNull ExecutionDetails executionDetails) {
        String podName;

        try {
            podName = kubernetesClient.createPod(executionDetails.getExperimentDetails().getId(),
                    executionDetails.getName());
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "There was an error while " +
                    "communicating with the cluster.", e);
        }

        executionDetails.setPodName(podName);
        executionDetails.setStatus(ExecutionDetails.Status.RUNNING);
        executionDetails.setStartedAt(LocalDateTime.now());

        executionRepository.save(executionDetails);

        return executionDetails;
    }

    private ExecutionDetails getExecutionDetails(@NonNull UUID executionId) {
        Optional<ExecutionDetails> executionDetailsById =
                executionRepository.findById(executionId);

        final ExecutionDetails execution = executionDetailsById.orElseThrow(
                () -> new ResourceNotFoundException(ExperimentDetails.class, "id",
                        executionId.toString()));

        execution.validatePermissions();
        return execution;
    }

    private ExecutionDetails createExecutionFromExecCreateDTO(@NonNull ExecutionCreateDTO execCreateDTO) {
        Optional<ExperimentDetails> experimentDetailsById =
                experimentRepository.findById(execCreateDTO.getExperimentId());

        final ExperimentDetails experiment = experimentDetailsById.orElseThrow(
                () -> new ResourceNotFoundException(ExperimentDetails.class, "id",
                        execCreateDTO.getExperimentId().toString()));

        experiment.validatePermissions();

        final String name;
        final long ram;
        final long cpu;
        final long bookedTime;

        if (!execCreateDTO.getName().isPresent()) {
            name = experiment.getName() + " #" + (experiment.getExecutionDetails().size() + 1);
        } else {
            name = execCreateDTO.getName().get();
        }

        if (!execCreateDTO.getRam().isPresent() || execCreateDTO.getRam().get() <= 0) {
            ram = ramDefault;
        } else {
            ram = execCreateDTO.getRam().get();
        }

        if (!execCreateDTO.getCpu().isPresent() || execCreateDTO.getCpu().get() <= 0) {
            cpu = cpuDefault;
        } else {
            cpu = execCreateDTO.getCpu().get();
        }

        if (!execCreateDTO.getBookedTime().isPresent() || execCreateDTO.getBookedTime().get() <= 0) {
            bookedTime = bookedTimeDefault;
        } else {
            bookedTime = execCreateDTO.getBookedTime().get();
        }

        return new ExecutionDetails(SecurityContextUtil.getCurrentUserDTO().getId(), experiment, name, ram, cpu,
                bookedTime);
    }
}
