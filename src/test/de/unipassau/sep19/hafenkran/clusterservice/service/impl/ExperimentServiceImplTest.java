package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.config.JwtAuthentication;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExperimentDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.UserDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class ExperimentServiceImplTest {

    private static final UUID MOCK_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static final UserDTO MOCK_USER = new UserDTO(MOCK_ID, "Rick", "", false);

    private static final JwtAuthentication MOCK_AUTH = new JwtAuthentication(MOCK_USER);

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private ExperimentRepository experimentRepository;

    private ExperimentDetails mockExperimentDetails;

    private ExperimentServiceImpl subject;

    @Mock
    private SecurityContext mockContext;

    @Before
    public void setUp() {
        this.subject = new ExperimentServiceImpl(experimentRepository);
        this.mockExperimentDetails = new ExperimentDetails(MOCK_ID, "testExperiment", 500);

        SecurityContextHolder.setContext(mockContext);
    }

    @Test
    public void testCreateExperiment_validExperimentDetails_validExperimentDetailsReturned() {

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
    }

    @Test
    public void testRetrieveExperimentDTOById_existingId_validExperimentDTO() {

        // Arrange
        ExperimentDTO mockExperimentDTO = ExperimentDTO.fromExperimentDetails(mockExperimentDetails);
        when(experimentRepository.findById(MOCK_ID)).thenReturn(Optional.ofNullable(mockExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExperimentDTO actual = subject.retrieveExperimentDTOById(MOCK_ID);

        // Assert
        verify(experimentRepository, times(1)).findById(MOCK_ID);
        verify(mockContext, times(1)).getAuthentication();
        assertEquals(actual, mockExperimentDTO);
        verifyNoMoreInteractions(experimentRepository, mockContext);
    }


    @Test
    public void testRetrieveExperimentDTOById_notOwnerOrAdmin_throwsException() {

        // Arrange
        UserDTO wrongUser = new UserDTO(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Rick", "", false);
        JwtAuthentication auth = new JwtAuthentication(wrongUser);
        when(mockContext.getAuthentication()).thenReturn(auth);

        expectedEx.expect(ResourceNotFoundException.class);
        when(experimentRepository.findById(MOCK_ID)).thenReturn(Optional.of(mockExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(auth);

        // Act
        ExperimentDTO actual = subject.retrieveExperimentDTOById(MOCK_ID);

        // Assert - with rule
    }

    @Test
    public void testRetrieveExperimentDTOById_noExistingId_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        when(experimentRepository.findById(MOCK_ID)).thenReturn(Optional.empty());

        // Act
        ExperimentDTO actual = subject.retrieveExperimentDTOById(MOCK_ID);

        // Assert - with rule

    }

    @Test
    public void testRetrieveExperimentDTOById_idIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("id is marked non-null but is null");

        // Act
        ExperimentDTO actual = subject.retrieveExperimentDTOById(null);

        // Assert - with rule

    }
}
