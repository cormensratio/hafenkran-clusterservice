package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExecutionRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static UUID MOCK_EXPERIMENT_ID;

    private static UUID MOCK_EXECUTION_ID;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private ExecutionRepository executionRepository;

    /*@Mock
    private ExecutionRepository executionRepositoryForExperimentDetails;*/

    private ExecutionDetails mockExecutionDetails;

    private ExperimentDetails experimentDetails;

    private List<ExecutionDetails> mockExecutionDetailsList;

    private ExecutionServiceImpl subject;

    @Before
    public void setUp() {
        this.subject = new ExecutionServiceImpl(executionRepository);
        experimentDetails = new ExperimentDetails(USER_ID, "testExperiment", 500);
        MOCK_EXPERIMENT_ID = experimentDetails.getId();
        this.mockExecutionDetails = new ExecutionDetails(experimentDetails);
        MOCK_EXECUTION_ID = mockExecutionDetails.getId();
        this.mockExecutionDetailsList = experimentDetails.getExecutionDetailsList();
    }

    @Test
    public void testCreateExecution_existingExecutionDetails_validExecutionDetailsReturned() {

        // Arrange
        when(executionRepository.save(mockExecutionDetails)).thenReturn(mockExecutionDetails);

        // Act
        ExecutionDetails actual = subject.createExecution(mockExecutionDetails);

        // Assert
        verify(executionRepository, times(1)).save(mockExecutionDetails);
        assertEquals(mockExecutionDetails, actual);
        verifyNoMoreInteractions(executionRepository);
    }

    @Test
    public void testCreateExecution_executionDetailsIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("executionDetails is marked non-null but is null");

        // Act
        ExecutionDetails actual = subject.createExecution(null);

        // Assert - with rule

    }

    @Test
    public void testFindExecutionById_existingId_validExecutionDetailsReturned() {

        // Arrange
        when(executionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.of(mockExecutionDetails));

        // Act
        ExecutionDetails actual = subject.findExecutionById(MOCK_EXECUTION_ID);

        // Assert
        verify(executionRepository, times(1)).findById(MOCK_EXECUTION_ID);
        assertEquals(mockExecutionDetails, actual);
        verifyNoMoreInteractions(executionRepository);
    }

    @Test
    public void testFindExecutionById_noExistingId_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        when(executionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.empty());

        // Act
        ExecutionDetails actual = subject.findExecutionById(MOCK_EXECUTION_ID);

        // Assert - with rule

    }

    @Test
    public void testFindExecutionById_idIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("id is marked non-null but is null");

        // Act
        ExecutionDetails actual = subject.findExecutionById(null);

        // Assert - with rule

    }

    @Test
    public void testFindExecutionDTOById_existingId_validExecutionDTO() {

        // Arrange
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(mockExecutionDetails);
        when(executionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.ofNullable(mockExecutionDetails));

        // Act
        ExecutionDTO actual = subject.findExecutionDTOById(MOCK_EXECUTION_ID);

        // Assert
        verify(executionRepository, times(1)).findById(MOCK_EXECUTION_ID);
        assertEquals(actual, mockExecutionDTO);
        verifyNoMoreInteractions(executionRepository);
    }

    @Test
    public void testFindExecutionDTOById_noExistingId_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        when(executionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.empty());

        // Act
        ExecutionDTO actual = subject.findExecutionDTOById(MOCK_EXECUTION_ID);

        // Assert - with rule

    }

    @Test
    public void testFindExecutionDTOById_idIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("id is marked non-null but is null");

        // Act
        ExecutionDTO actual = subject.findExecutionDTOById(null);

        // Assert - with rule

    }

    @Test
    public void testFindExecutionsDTOListOfExperimentId_existingId_validExecutionDTOList() {

        // Arrange
        List<ExecutionDetails> mockExecutionDetailsListTest = new ArrayList<>();
        mockExecutionDetailsListTest.add(mockExecutionDetails);
        when(executionRepository.findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID)).thenReturn(mockExecutionDetailsListTest);
        experimentDetails.setExecutionDetailsList(mockExecutionDetailsListTest);

        // Act
        List<ExecutionDTO> actual = subject.findExecutionsDTOListOfExperimentId(MOCK_EXPERIMENT_ID);

        // Assert
        verify(executionRepository, times(1)).findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID);
        assertEquals(actual, mockExecutionDetailsListTest);
        verifyNoMoreInteractions(executionRepository);
    }

    @Test
    public void testFindExecutionsDTOListOfExperimentId_noExistingId_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        when(executionRepository.findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID)).thenReturn(mockExecutionDetailsList);

        // Act
        List<ExecutionDTO> actual = subject.findExecutionsDTOListOfExperimentId(MOCK_EXPERIMENT_ID);

        // Assert - with rule

    }

    @Test
    public void testFindExecutionsDTOListOfExperimentId_experimentIdIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("experimentId is marked non-null but is null");

        // Act
        List<ExecutionDTO> actual = subject.findExecutionsDTOListOfExperimentId(null);

        // Assert - with rule

    }

    @Test
    public void testFindExecutionsDTOListForUserId_existingUserId_validExecutionDTOList() {

        // Arrange
        List<ExecutionDetails> mockExecutionDetailsListTest = new ArrayList<>();
        mockExecutionDetailsListTest.add(mockExecutionDetails);
        when(executionRepository.findAllByExperimentDetails_UserId(USER_ID)).thenReturn(mockExecutionDetailsListTest);
        experimentDetails.setExecutionDetailsList(mockExecutionDetailsListTest);

        // Act
        List<ExecutionDTO> actual = subject.findExecutionsDTOListForUserId(USER_ID);

        // Assert
        verify(executionRepository, times(1)).findAllByExperimentDetails_UserId(USER_ID);
        assertEquals(actual, mockExecutionDetailsListTest);
        verifyNoMoreInteractions(executionRepository);
    }

    @Test
    public void testFindExecutionsDTOListForUserId_noExistingUserId_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        when(executionRepository.findAllByExperimentDetails_UserId(USER_ID)).thenReturn(mockExecutionDetailsList);

        // Act
        List<ExecutionDTO> actual = subject.findExecutionsDTOListForUserId(USER_ID);

        // Assert - with rule

    }

    @Test
    public void testFindExecutionsDTOListForUserId_userIdIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("userId is marked non-null but is null");

        // Act
        List<ExecutionDTO> actual = subject.findExecutionsDTOListForUserId(null);

        // Assert - with rule

    }

}
