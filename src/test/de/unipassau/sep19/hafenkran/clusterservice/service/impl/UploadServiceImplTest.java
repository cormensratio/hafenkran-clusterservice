package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.UploadService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UploadServiceImplTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    private UploadService subject;
    private ExperimentDetails mockExperimentDetails;
    private MultipartFile mockFile;
    private ExperimentDTO mockExperimentDTO;
    private String mockPath;

    @Before
    public void setUp() {
        this.subject = new UploadServiceImpl();
        this.mockExperimentDetails = new ExperimentDetails(ID, "testExperiment", 500);
        this.mockFile = new MockMultipartFile(mockExperimentDetails.getExperimentName(), (byte[]) null);
        this.mockExperimentDTO = new ExperimentDTO(mockExperimentDetails);
        this.mockPath = "temp/hafenkran-clusterservice/test";
    }

    @Test
    public void testStoreFile_existingFile_existingExperimentDetails_validExperimentDTOReturned() {

        // Arrange
        // The experimentsFileUploadPath is not configured correctly
        when(subject.storeFile(mockFile, mockExperimentDetails)).thenReturn(mockExperimentDTO);

        // Act
        ExperimentDTO actual = subject.storeFile(mockFile, mockExperimentDetails);

        // Assert
        verify(subject, times(1)).storeFile(mockFile, mockExperimentDetails);
        Assertions.assertEquals(mockExperimentDTO, actual);
        Assertions.assertNotNull(mockExperimentDTO.getId());
        verifyNoMoreInteractions(subject);
    }

    @Test
    public void testStoreFile_FileIsNull_existingExperimentDetails_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("file is marked non-null but is null");

        // Act
        ExperimentDTO actual = subject.storeFile(null, mockExperimentDetails);

        // Assert - with rule

    }

    @Test
    public void testStoreFile_existingFile_ExperimentDetailsAreNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("experimentDetails are marked non-null but are null");

        // Act
        ExperimentDTO actual = subject.storeFile(mockFile, null);

        // Assert - with rule

    }

    @Test
    public void testStoreFile_FileIsNull_ExperimentDetailsAreNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("file and experimentDetails are marked non-null but are null");

        // Act
        ExperimentDTO actual = subject.storeFile(null, null);

        // Assert - with rule

    }

}
