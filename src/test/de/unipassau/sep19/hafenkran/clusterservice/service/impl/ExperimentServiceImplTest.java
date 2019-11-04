package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import de.unipassau.sep19.hafenkran.clusterservice.repository.UserRepository;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExperimentService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class ExperimentServiceImplTest {

    private static final UUID MOCK_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private ExperimentRepository experimentRepository;

    private UserRepository userRepository;
    private ExperimentService subject;
    private ExperimentDetails mockExperimentDetails;

    @Before
    public void setUp() {
        this.subject = new ExperimentServiceImpl(experimentRepository, userRepository);
        mockExperimentDetails = new ExperimentDetails(MOCK_ID, "testExperiment", 500);
    }

    @Test
    public void testGetExperimentById_existingId_validUserDetailsReturned() {

        // Arrange
        when(experimentRepository.findById(MOCK_ID)).thenReturn(Optional.of(mockExperimentDetails));

        // Act
        ExperimentDetails actual = subject.getExperimentById(MOCK_ID);

        // Assert
        verify(experimentRepository, times(1)).findById(MOCK_ID);
        assertEquals(mockExperimentDetails, actual);
        verifyNoMoreInteractions(experimentRepository);
    }

    @Test
    public void testGetExperimentById_noExistingId_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        when(experimentRepository.findById(MOCK_ID)).thenReturn(Optional.empty());

        // Act
        ExperimentDetails actual = subject.getExperimentById(MOCK_ID);

        // Assert - with rule
        verify(experimentRepository, times(1)).findById(MOCK_ID);
        Assertions.assertNull(actual);
        verifyNoMoreInteractions(experimentRepository);
    }

    @Test
    public void testGetExperimentById_idIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("id is marked non-null but is null");

        // Act
        ExperimentDetails actual = subject.getExperimentById(null);

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

        // Arrange - with rule
        verifyNoMoreInteractions(experimentRepository);
    }
}
