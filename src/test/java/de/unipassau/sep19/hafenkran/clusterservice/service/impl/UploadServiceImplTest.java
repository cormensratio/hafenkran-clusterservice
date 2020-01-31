package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import de.unipassau.sep19.hafenkran.clusterservice.service.MetricsService;
import de.unipassau.sep19.hafenkran.clusterservice.service.UploadService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;


@RunWith(MockitoJUnitRunner.class)
public class UploadServiceImplTest {

    private static final byte[] bytearray = new byte[]{(byte) 0xe0, 0x4f, (byte) 0xd0,
            0x20, (byte) 0xea, 0x3a, 0x69, 0x10, (byte) 0xa2, (byte) 0xd8, 0x08, 0x00, 0x2b,
            0x30, 0x30, (byte) 0x9d};

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private UploadService subject;

    @Mock
    ExperimentService experimentService;

    @Mock
    ExperimentRepository experimentRepository;
    private MultipartFile testFile;

    @Mock
    KubernetesClient kubernetesClient;

    @Mock
    MetricsService metricsService;

    @Before
    public void setUp() {
        this.subject = new UploadServiceImpl(experimentService, kubernetesClient, experimentRepository, metricsService);
        this.testFile = new MockMultipartFile("testExperiment", "testfile", "text/txt", bytearray);
    }

    @Test
    public void testStoreFile_FileIsNull_existingExperimentDetails_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("file is marked non-null but is null");

        // Act
        ExperimentDTO actual = subject.storeFile(null, "testExperiment");

        // Assert - with rule

    }

    @Test
    public void testStoreFile_existingFile_ExperimentDetailsIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("experimentName is marked non-null but is null");

        // Act
        ExperimentDTO actual = subject.storeFile(testFile, null);

        // Assert - with rule

    }

}
