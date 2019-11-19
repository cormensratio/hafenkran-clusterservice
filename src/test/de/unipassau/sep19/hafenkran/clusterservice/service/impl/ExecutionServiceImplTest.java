package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.config.JwtAuthentication;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.UserDTO;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionServiceImplTest {

    private static final UUID MOCK_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UserDTO MOCK_USER = new UserDTO(MOCK_USER_ID, "Rick", "", false);
    private static final JwtAuthentication MOCK_AUTH = new JwtAuthentication(MOCK_USER);
    private static UUID MOCK_EXPERIMENT_ID;
    private static UUID MOCK_EXECUTION_ID;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private ExecutionRepository mockExecutionRepository;

    @Mock
    private ExperimentRepository mockExperimentRepository;

    @Mock
    private SecurityContext mockContext;

    private ExperimentDetails mockExperimentDetails;

    private ExecutionDetails mockExecutionDetails;

    private List<ExecutionDetails> mockExecutionDetailsList;

    private List<ExecutionDTO> mockExecutionDTOS;

    private ExecutionServiceImpl subject;

    @Before
    public void setUp() {
        SecurityContextHolder.setContext(mockContext);

        this.subject = new ExecutionServiceImpl(mockExecutionRepository, mockExperimentRepository);

        ExperimentDetails experimentDetails = new ExperimentDetails(MOCK_USER_ID, "testExperiment", 500);
        experimentDetails.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        MOCK_EXPERIMENT_ID = experimentDetails.getId();

        this.mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "ExpTest", 1L);

        this.mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, experimentDetails, "Test1", 1L, 1L, 1L);
        MOCK_EXECUTION_ID = mockExecutionDetails.getId();

        this.mockExecutionDetailsList = new ArrayList<>();
        this.mockExecutionDTOS = new ArrayList<>();
    }

    @Test
    public void testRetrieveExecutionDTOById_existingId_validExecutionDTO() {

        // Arrange
        ExecutionDTO mockExecutionDTO = ExecutionDTO.fromExecutionDetails(mockExecutionDetails);
        when(mockExecutionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.ofNullable(mockExecutionDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExecutionDTO actual = subject.retrieveExecutionDTOById(MOCK_EXECUTION_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findById(MOCK_EXECUTION_ID);
        verify(mockContext, times(1)).getAuthentication();
        assertEquals(mockExecutionDTO, actual);
        verifyNoMoreInteractions(mockExecutionRepository, mockContext);
    }

    @Test
    public void testRetrieveExecutionDTOById_noExistingId_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        when(mockExecutionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.empty());

        // Act
        ExecutionDTO actual = subject.retrieveExecutionDTOById(MOCK_EXECUTION_ID);

        // Assert - with rule

    }

    @Test
    public void testRetrieveExecutionDTOById_idIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("id is marked non-null but is null");

        // Act
        ExecutionDTO actual = subject.retrieveExecutionDTOById(null);

        // Assert - with rule

    }

    @Test
    public void testRetrieveExecutionsDTOListOfExperimentId_existingId_validExecutionDTOList() {

        // Arrange
        mockExecutionDTOS.add(new ExecutionDTO(mockExecutionDetails.getId(),
                mockExecutionDetails.getExperimentDetails().getId(), mockExecutionDetails.getExecutionName(),
                mockExecutionDetails.getCreatedAt(),
                mockExecutionDetails.getStartedAt(), mockExecutionDetails.getTerminatedAt(),
                mockExecutionDetails.getStatus(), mockExecutionDetails.getRam(), mockExecutionDetails.getCpu(),
                mockExecutionDetails.getBookedTime()));
        mockExecutionDetailsList.add(mockExecutionDetails);
        when(mockExecutionRepository.findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID)).thenReturn(
                mockExecutionDetailsList);
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        List<ExecutionDTO> actual = subject.retrieveExecutionsDTOListOfExperimentId(MOCK_EXPERIMENT_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID);
        verify(mockContext, times(1)).getAuthentication();
        assertEquals(mockExecutionDTOS, actual);
        verifyNoMoreInteractions(mockExecutionRepository, mockContext);
    }

    @Test
    public void testRetrieveExecutionsDTOListOfExperimentId_noExistingId_emptyExecutionDetailsList() {

        // Arrange
        when(mockExecutionRepository.findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID)).thenReturn(
                mockExecutionDetailsList);

        // Act
        List<ExecutionDTO> actual = subject.retrieveExecutionsDTOListOfExperimentId(MOCK_EXPERIMENT_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID);
        assertEquals(mockExecutionDTOS, actual);
        verifyNoMoreInteractions(mockExecutionRepository);
    }

    @Test
    public void testRetrieveExecutionsDTOListOfExperimentId_experimentIdIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("experimentId is marked non-null but is null");

        // Act
        List<ExecutionDTO> actual = subject.retrieveExecutionsDTOListOfExperimentId(null);

        // Assert - with rule

    }

    @Test
    public void testRetrieveExecutionsDTOListOfExperimentId_validIdButEmptyExecutionList_emptyExecutionDetailsList() {

        // Arrange
        when(mockExecutionRepository.findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID)).thenReturn(
                Collections.emptyList());

        // Act
        List<ExecutionDTO> actual = subject.retrieveExecutionsDTOListOfExperimentId(MOCK_EXPERIMENT_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID);
        assertEquals(mockExecutionDTOS, actual);
        verifyNoMoreInteractions(mockExecutionRepository);
    }

    @Test
    public void testRetrieveExecutionsDTOListForUserId_existingUserId_validExecutionDTOList() {

        // Arrange
        mockExecutionDTOS.add(new ExecutionDTO(mockExecutionDetails.getId(),
                mockExecutionDetails.getExperimentDetails().getId(), mockExecutionDetails.getExecutionName(),
                mockExecutionDetails.getCreatedAt(),
                mockExecutionDetails.getStartedAt(), mockExecutionDetails.getTerminatedAt(),
                mockExecutionDetails.getStatus(), mockExecutionDetails.getRam(), mockExecutionDetails.getCpu(),
                mockExecutionDetails.getBookedTime()));
        mockExecutionDetailsList.add(mockExecutionDetails);
        when(mockExecutionRepository.findAllByExperimentDetails_UserId(MOCK_USER_ID)).thenReturn(
                mockExecutionDetailsList);
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        List<ExecutionDTO> actual = subject.retrieveExecutionsDTOListForUserId(MOCK_USER_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findAllByExperimentDetails_UserId(MOCK_USER_ID);
        verify(mockContext, times(1)).getAuthentication();
        assertEquals(mockExecutionDTOS, actual);
        verifyNoMoreInteractions(mockExecutionRepository, mockContext);
    }

    @Test
    public void testRetrieveExecutionsDTOListForUserId_noExecutionForUser_returnsEmptyList() {

        // Arrange


        // Act
        List<ExecutionDTO> executionDTOS = subject.retrieveExecutionsDTOListForUserId(MOCK_USER_ID);

        // Assert - with rule
        verify(mockExecutionRepository, times(1)).findAllByExperimentDetails_UserId(MOCK_USER_ID);
        assertThat(mockExecutionDTOS, empty());
        verifyNoMoreInteractions(mockExecutionRepository);
    }

    @Test
    public void testRetrieveExecutionsDTOListForUserId_userIdIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("userId is marked non-null but is null");

        // Act
        List<ExecutionDTO> actual = subject.retrieveExecutionsDTOListForUserId(null);

        // Assert - with rule

    }

    @Test
    public void testCreateExecution_invalidIdOfExecutionCreateDTO_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_EXPERIMENT_ID,
                Optional.empty(), Optional.empty(), Optional.empty());
        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.empty());

        // Act
        ExecutionDTO actual = subject.createExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateExecution_validExecutionCreateDTOWithAllOptionalFieldsEmpty_validExecutionDTOWithDefaultValues() {

        // Arrange
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_EXPERIMENT_ID,
                Optional.empty(), Optional.empty(), Optional.empty());
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_EXECUTION_ID, MOCK_EXPERIMENT_ID, "Test1",
                LocalDateTime.now(), null, null, ExecutionDetails.Status.WAITING, 1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExecutionDTO actualExecutionDTO = subject.createExecution(executionCreateDTO);

        // Assert
        verify(mockExperimentRepository, times(1)).findById(MOCK_EXPERIMENT_ID);
        verify(mockExecutionRepository, times(1)).save(any(ExecutionDetails.class));
        verify(mockContext, times(2)).getAuthentication();
        assertEquals(mockExecutionDTO.getRam(), actualExecutionDTO.getRam());
        assertEquals(mockExecutionDTO.getCpu(), actualExecutionDTO.getCpu());
        assertEquals(mockExecutionDTO.getExecutionName(), actualExecutionDTO.getExecutionName());
        assertEquals(mockExecutionDTO.getBookedTime(), actualExecutionDTO.getBookedTime());
        assertEquals(mockExecutionDTO.getStatus(), actualExecutionDTO.getStatus());
        verifyNoMoreInteractions(mockExperimentRepository, mockExecutionRepository, mockContext);
    }

    @Test
    public void testCreateExecution_validExecutionCreateDTOWithAllOptionalFieldsSet_validExecutionDTOWithSetValues() {

        // Arrange
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.of("Test1"), MOCK_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_EXECUTION_ID, MOCK_EXPERIMENT_ID, "Test1",
                LocalDateTime.now(), null, null, ExecutionDetails.Status.WAITING, 1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExecutionDTO actualExecutionDTO = subject.createExecution(executionCreateDTO);

        // Assert
        verify(mockExperimentRepository, times(1)).findById(MOCK_EXPERIMENT_ID);
        verify(mockExecutionRepository, times(1)).save(any(ExecutionDetails.class));
        verify(mockContext, times(2)).getAuthentication();
        assertEquals(mockExecutionDTO.getRam(), actualExecutionDTO.getRam());
        assertEquals(mockExecutionDTO.getCpu(), actualExecutionDTO.getCpu());
        assertEquals(mockExecutionDTO.getExecutionName(), actualExecutionDTO.getExecutionName());
        assertEquals(mockExecutionDTO.getBookedTime(), actualExecutionDTO.getBookedTime());
        assertEquals(mockExecutionDTO.getStatus(), actualExecutionDTO.getStatus());
        verifyNoMoreInteractions(mockExperimentRepository, mockExecutionRepository, mockContext);

    }
}
