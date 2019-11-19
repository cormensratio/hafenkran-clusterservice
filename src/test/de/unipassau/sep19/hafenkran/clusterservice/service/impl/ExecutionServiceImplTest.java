package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExecutionRepository;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionServiceImplTest {

    private static final UUID MOCK_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static UUID MOCK_EXPERIMENT_ID;

    private static UUID MOCK_EXECUTION_ID;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    private ExperimentDetails mockExperimentDetails;

    private ExecutionDetails mockExecutionDetails;

    private ExecutionCreateDTO mockExecutionCreateDTOAllSet;

    private ExecutionCreateDTO mockExecutionCreateDTOAllUnset;

    private LocalDateTime mockLocalDateTime;

    private List<ExecutionDetails> mockExecutionDetailsList;

    private List<ExecutionDTO> mockExecutionDTOS;

    private ExecutionServiceImpl subject;

    @Before
    public void setUp() {
        this.subject = new ExecutionServiceImpl(executionRepository, experimentRepository);

        ExperimentDetails experimentDetails = new ExperimentDetails(MOCK_USER_ID, "testExperiment", 500);
        MOCK_EXPERIMENT_ID = experimentDetails.getId();

        this.mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "ExpTest", 1L);

        this.mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, experimentDetails, "Test1", 1L, 1L, 1L);
        MOCK_EXECUTION_ID = mockExecutionDetails.getId();

        this.mockExecutionCreateDTOAllSet = new ExecutionCreateDTO(MOCK_EXPERIMENT_ID, Optional.of("Test1"),
                Optional.of(1L), Optional.of(1L), Optional.of(1L));

        this.mockExecutionCreateDTOAllUnset = new ExecutionCreateDTO(MOCK_EXPERIMENT_ID, Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());

        this.mockLocalDateTime = LocalDateTime.now();

        this.mockExecutionDTOS = new ArrayList<>();

        this.mockExecutionDetailsList = new ArrayList<>();
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
        mockExecutionDetails = null;

        // Act
        ExecutionDetails actual = subject.createExecution(mockExecutionDetails);

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
        assertEquals(mockExecutionDTO, actual);
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
        mockExecutionDTOS.add(new ExecutionDTO(mockExecutionDetails.getId(),
                mockExecutionDetails.getExperimentDetails().getId(), mockExecutionDetails.getExecutionName(),
                mockExecutionDetails.getCreatedAt(),
                mockExecutionDetails.getStartedAt(), mockExecutionDetails.getTerminatedAt(),
                mockExecutionDetails.getStatus(), mockExecutionDetails.getRam(), mockExecutionDetails.getCpu(),
                mockExecutionDetails.getBookedTime()));
        mockExecutionDetailsList.add(mockExecutionDetails);
        when(executionRepository.findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID)).thenReturn(
                mockExecutionDetailsList);

        // Act
        List<ExecutionDTO> actual = subject.findExecutionsDTOListOfExperimentId(MOCK_EXPERIMENT_ID);

        // Assert
        verify(executionRepository, times(1)).findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID);
        assertEquals(mockExecutionDTOS, actual);
        verifyNoMoreInteractions(executionRepository);
    }

    @Test
    public void testFindExecutionsDTOListOfExperimentId_noExistingId_emptyExecutionDetailsList() {

        // Arrange
        when(executionRepository.findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID)).thenReturn(
                mockExecutionDetailsList);

        // Act
        List<ExecutionDTO> actual = subject.findExecutionsDTOListOfExperimentId(MOCK_EXPERIMENT_ID);

        // Assert
        verify(executionRepository, times(1)).findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID);
        assertEquals(mockExecutionDTOS, actual);
        verifyNoMoreInteractions(executionRepository);
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
    public void testFindExecutionsDTOListOfExperimentId_validIdButEmptyExecutionList_emptyExecutionDetailsList() {

        // Arrange
        when(executionRepository.findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID)).thenReturn(Collections.emptyList());

        // Act
        List<ExecutionDTO> actual = subject.findExecutionsDTOListOfExperimentId(MOCK_EXPERIMENT_ID);

        // Assert
        verify(executionRepository, times(1)).findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID);
        assertEquals(mockExecutionDTOS, actual);
        verifyNoMoreInteractions(executionRepository);
    }

    @Test
    public void testFindExecutionsDTOListForUserId_existingUserId_validExecutionDTOList() {

        // Arrange
        mockExecutionDTOS.add(new ExecutionDTO(mockExecutionDetails.getId(),
                mockExecutionDetails.getExperimentDetails().getId(), mockExecutionDetails.getExecutionName(),
                mockExecutionDetails.getCreatedAt(),
                mockExecutionDetails.getStartedAt(), mockExecutionDetails.getTerminatedAt(),
                mockExecutionDetails.getStatus(), mockExecutionDetails.getRam(), mockExecutionDetails.getCpu(),
                mockExecutionDetails.getBookedTime()));
        mockExecutionDetailsList.add(mockExecutionDetails);
        when(executionRepository.findAllByExperimentDetails_UserId(MOCK_USER_ID)).thenReturn(mockExecutionDetailsList);

        // Act
        List<ExecutionDTO> actual = subject.findExecutionsDTOListForUserId(MOCK_USER_ID);

        // Assert
        verify(executionRepository, times(1)).findAllByExperimentDetails_UserId(MOCK_USER_ID);
        assertEquals(mockExecutionDTOS, actual);
        verifyNoMoreInteractions(executionRepository);
    }

    @Test
    public void testFindExecutionsDTOListForUserId_noExistingUserId_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);

        // Act
        List<ExecutionDTO> actual = subject.findExecutionsDTOListForUserId(MOCK_USER_ID);

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

    @Test
    public void testCreateExecution_invalidIdOfExecutionCreateDTO_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        when(experimentRepository.findById(mockExecutionCreateDTOAllSet.getExperimentId())).thenReturn(
                Optional.empty());

        // Act
        ExecutionDTO actual = subject.createExecution(mockExecutionCreateDTOAllSet);

        // Assert - with rule

    }

    @Test
    public void testCreateExecution_validExecutionCreateDTOWithAllOptionalFieldsEmpty_validExecutionDTOWithDefaultValues() {

        // Arrange
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_EXECUTION_ID, MOCK_EXPERIMENT_ID, "Test1",
                mockLocalDateTime, null, null, ExecutionDetails.Status.WAITING, 1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        when(experimentRepository.findById(mockExecutionCreateDTOAllUnset.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(executionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);

        // Act
        ExecutionDTO actualExecutionDTO = subject.createExecution(mockExecutionCreateDTOAllUnset);

        // Assert
        verify(experimentRepository, times(1)).findById(MOCK_EXPERIMENT_ID);
        verify(executionRepository, times(1)).save(any(ExecutionDetails.class));
        assertEquals(mockExecutionDTO.getRam(), actualExecutionDTO.getRam());
        assertEquals(mockExecutionDTO.getCpu(), actualExecutionDTO.getCpu());
        assertEquals(mockExecutionDTO.getExecutionName(), actualExecutionDTO.getExecutionName());
        assertEquals(mockExecutionDTO.getBookedTime(), actualExecutionDTO.getBookedTime());
        assertEquals(mockExecutionDTO.getStatus(), actualExecutionDTO.getStatus());
        verifyNoMoreInteractions(experimentRepository, executionRepository);
    }

    @Test
    public void testCreateExecution_validExecutionCreateDTOWithAllOptionalFieldsSet_validExecutionDTOWithSetValues() {

        // Arrange
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_EXECUTION_ID, MOCK_EXPERIMENT_ID, "Test1",
                mockLocalDateTime, null, null, ExecutionDetails.Status.WAITING, 1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        when(experimentRepository.findById(mockExecutionCreateDTOAllSet.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(executionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);

        // Act
        ExecutionDTO actualExecutionDTO = subject.createExecution(mockExecutionCreateDTOAllSet);

        // Assert
        verify(experimentRepository, times(1)).findById(MOCK_EXPERIMENT_ID);
        verify(executionRepository, times(1)).save(any(ExecutionDetails.class));
        assertEquals(mockExecutionDTO.getRam(), actualExecutionDTO.getRam());
        assertEquals(mockExecutionDTO.getCpu(), actualExecutionDTO.getCpu());
        assertEquals(mockExecutionDTO.getExecutionName(), actualExecutionDTO.getExecutionName());
        assertEquals(mockExecutionDTO.getBookedTime(), actualExecutionDTO.getBookedTime());
        assertEquals(mockExecutionDTO.getStatus(), actualExecutionDTO.getStatus());
        verifyNoMoreInteractions(experimentRepository, executionRepository);

    }
}
