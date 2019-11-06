package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class ExperimentServiceImplTest {

    private static final UUID MOCK_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private ExperimentDetails mockExperimentDetails;

    @Mock
    private List<ExperimentDetails> mockExperimentDetailsList;

    @Mock
    private List<ExperimentDTO> mockExperimentDTOList;

    @InjectMocks
    private ExperimentServiceImpl subject;


    @Before
    public void setUp() {
        mockExperimentDetails = new ExperimentDetails(MOCK_ID, "testExperiment", 500);
    }

    @Test
    public void testFindExperimentById_existingId_validExperimentDetailsReturned() {

        // Arrange
        when(experimentRepository.findById(MOCK_ID)).thenReturn(Optional.of(mockExperimentDetails));

        // Act
        ExperimentDetails actual = subject.findExperimentById(MOCK_ID);

        // Assert
        verify(experimentRepository, times(1)).findById(MOCK_ID);
        assertEquals(mockExperimentDetails, actual);
        verifyNoMoreInteractions(experimentRepository);
    }

    @Test
    public void testFindExperimentById_noExistingId_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        when(experimentRepository.findById(MOCK_ID)).thenReturn(Optional.empty());

        // Act
        ExperimentDetails actual = subject.findExperimentById(MOCK_ID);

        // Assert - with rule
        verify(experimentRepository, times(1)).findById(MOCK_ID);
        Assertions.assertNull(actual);
        verifyNoMoreInteractions(experimentRepository);
    }

    @Test
    public void testFindExperimentById_idIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("id is marked non-null but is null");

        // Act
        ExperimentDetails actual = subject.findExperimentById(null);

        // Arrange - with rule
        verifyNoMoreInteractions(experimentRepository);

    }

    @Test
    public void testCreateExperiment_existingExperimentDetails_validExperimentDetailsReturned() {

        // Arrange
        when(experimentRepository.save(mockExperimentDetails)).thenReturn(mockExperimentDetails);

        // Act
        ExperimentDetails actual = subject.createExperiment(mockExperimentDetails);

        // Assert
        verify(experimentRepository, times(1)).save(mockExperimentDetails);
        assertEquals(mockExperimentDetails, actual);
        verifyNoMoreInteractions(experimentRepository);
    }

    @Test
    public void testCreateExperiment_experimentDetailsIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("experimentDetails is marked non-null but is null");

        // Act
        ExperimentDetails actual = subject.createExperiment(null);

        // Assert - with rule
        verifyNoMoreInteractions(experimentRepository);
    }

    @Test
    public void testFindExperimentDTOById_existingId_validExperimentDTO() {

        // Arrange
        ExperimentDTO mockExperimentDTO = new ExperimentDTO(mockExperimentDetails);
        when(experimentRepository.findById(MOCK_ID)).thenReturn(Optional.ofNullable(mockExperimentDetails));

        // Act
        ExperimentDTO actual = subject.findExperimentDTOById(MOCK_ID);

        // Assert
        verify(experimentRepository, times(1)).findById(MOCK_ID);
        assertEquals(actual, mockExperimentDTO);
        verifyNoMoreInteractions(experimentRepository);
    }

    @Test
    public void testFindExperimentDTOById_noExistingId_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        when(experimentRepository.findById(MOCK_ID)).thenReturn(Optional.empty());

        // Act
        ExperimentDTO actual = subject.findExperimentDTOById(MOCK_ID);

        // Assert
        verify(experimentRepository, times(1)).findById(MOCK_ID);
        assertNull(actual);
        verifyNoMoreInteractions(experimentRepository);

    }

    @Test
    public void testFindExperimentDTOById_idIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("id is marked non-null but is null");

        // Act
        ExperimentDTO actual = subject.findExperimentDTOById(null);

        // Assert - with rule
        verifyNoMoreInteractions(experimentRepository);
    }
}
