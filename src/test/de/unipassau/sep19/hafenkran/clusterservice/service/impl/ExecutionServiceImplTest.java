package de.unipassau.sep19.hafenkran.clusterservice.service.impl;

import de.unipassau.sep19.hafenkran.clusterservice.config.JwtAuthentication;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionCreateDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.ExecutionDTO;
import de.unipassau.sep19.hafenkran.clusterservice.dto.UserDTO;
import de.unipassau.sep19.hafenkran.clusterservice.exception.ResourceNotFoundException;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExperimentDetails;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExecutionRepository;
import de.unipassau.sep19.hafenkran.clusterservice.repository.ExperimentRepository;
import io.kubernetes.client.ApiException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

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
    private static final UUID MOCK_EXPERIMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MOCK_EXECUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private ExecutionRepository mockExecutionRepository;

    @Mock
    private ExperimentRepository mockExperimentRepository;

    @Mock
    private KubernetesClient mockKubernetesClient;

    @Mock
    private SecurityContext mockContext;

    private ExperimentDetails testExperimentDetails;

    private ExecutionDetails testExecutionDetails;

    private List<ExecutionDetails> testExecutionDetailsList;

    private List<ExecutionDTO> testExecutionDTOS;

    private ExecutionServiceImpl subject;

    @Before
    public void setUp() {
        SecurityContextHolder.setContext(mockContext);

        this.subject = new ExecutionServiceImpl(mockExecutionRepository, mockExperimentRepository, mockKubernetesClient);

        ExperimentDetails experimentDetails = new ExperimentDetails(MOCK_USER_ID, "testExperiment", 500);
        experimentDetails.setId(MOCK_EXPERIMENT_ID);

        this.testExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "ExpTest", 1L);
        testExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        this.testExecutionDetails = new ExecutionDetails(MOCK_USER_ID, experimentDetails, "Test1", 1L, 1L, 1L);
        testExecutionDetails.setId(MOCK_EXECUTION_ID);

        this.testExecutionDetailsList = new ArrayList<>();
        this.testExecutionDTOS = new ArrayList<>();
    }

    @Test
    public void testRetrieveExecutionDTOById_existingId_validExecutionDTO() {

        // Arrange
        ExecutionDTO mockExecutionDTO = ExecutionDTO.fromExecutionDetails(testExecutionDetails);
        when(mockExecutionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.ofNullable(testExecutionDetails));
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
    public void testRetrieveExecutionDTOById_existingIdAndNotOwnerOfExecution_notFoundException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);

        UserDTO notOwner = new UserDTO(UUID.fromString("00000000-0000-0000-0000-000000000042"),
                "Rick", "", false);
        JwtAuthentication notOwnerAuth = new JwtAuthentication(notOwner);
        ExecutionDTO mockExecutionDTO = ExecutionDTO.fromExecutionDetails(testExecutionDetails);

        when(mockExecutionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.ofNullable(testExecutionDetails));
        when(mockContext.getAuthentication()).thenReturn(notOwnerAuth);

        // Act
        ExecutionDTO actual = subject.retrieveExecutionDTOById(MOCK_EXECUTION_ID);

        // Assert -- with rule
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
        testExecutionDTOS.add(new ExecutionDTO(testExecutionDetails.getId(),
                testExecutionDetails.getExperimentDetails().getId(), testExecutionDetails.getName(),
                testExecutionDetails.getCreatedAt(),
                testExecutionDetails.getStartedAt(), testExecutionDetails.getTerminatedAt(),
                testExecutionDetails.getStatus(), testExecutionDetails.getRam(), testExecutionDetails.getCpu(),
                testExecutionDetails.getBookedTime()));
        testExecutionDetailsList.add(testExecutionDetails);
        when(mockExecutionRepository.findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID)).thenReturn(
                testExecutionDetailsList);
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        List<ExecutionDTO> actual = subject.retrieveExecutionsDTOListOfExperimentId(MOCK_EXPERIMENT_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID);
        verify(mockContext, times(1)).getAuthentication();
        assertEquals(testExecutionDTOS, actual);
        verifyNoMoreInteractions(mockExecutionRepository, mockContext);
    }

    @Test
    public void testRetrieveExecutionsDTOListOfExperimentId_noExistingId_emptyExecutionDetailsList() {

        // Arrange
        when(mockExecutionRepository.findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID)).thenReturn(
                testExecutionDetailsList);

        // Act
        List<ExecutionDTO> actual = subject.retrieveExecutionsDTOListOfExperimentId(MOCK_EXPERIMENT_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findAllByExperimentDetails_Id(MOCK_EXPERIMENT_ID);
        assertEquals(testExecutionDTOS, actual);
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
        assertEquals(testExecutionDTOS, actual);
        verifyNoMoreInteractions(mockExecutionRepository);
    }

    @Test
    public void testRetrieveExecutionsDTOListForUserId_existingUserId_validExecutionDTOList() {

        // Arrange
        testExecutionDTOS.add(new ExecutionDTO(testExecutionDetails.getId(),
                testExecutionDetails.getExperimentDetails().getId(), testExecutionDetails.getName(),
                testExecutionDetails.getCreatedAt(),
                testExecutionDetails.getStartedAt(), testExecutionDetails.getTerminatedAt(),
                testExecutionDetails.getStatus(), testExecutionDetails.getRam(), testExecutionDetails.getCpu(),
                testExecutionDetails.getBookedTime()));
        testExecutionDetailsList.add(testExecutionDetails);
        when(mockExecutionRepository.findAllByExperimentDetails_OwnerId(MOCK_USER_ID)).thenReturn(
                testExecutionDetailsList);
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        List<ExecutionDTO> actual = subject.retrieveExecutionsDTOListForUserId(MOCK_USER_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findAllByExperimentDetails_OwnerId(MOCK_USER_ID);
        verify(mockContext, times(1)).getAuthentication();
        assertEquals(testExecutionDTOS, actual);
        verifyNoMoreInteractions(mockExecutionRepository, mockContext);
    }

    @Test
    public void testRetrieveExecutionsDTOListForUserId_noExecutionForUser_returnsEmptyList() {

        // Arrange


        // Act
        List<ExecutionDTO> executionDTOS = subject.retrieveExecutionsDTOListForUserId(MOCK_USER_ID);

        // Assert - with rule
        verify(mockExecutionRepository, times(1)).findAllByExperimentDetails_OwnerId(MOCK_USER_ID);
        assertThat(testExecutionDTOS, empty());
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
    public void testCreateAndStartExecution_validExecutionCreateDTOWithAllOptionalFieldsEmpty_validExecutionDTOWithDefaultValues() {

        // Arrange
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_EXPERIMENT_ID,
                Optional.empty(), Optional.empty(), Optional.empty());
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "Test", 1L);
        mockExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1", 1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_EXECUTION_ID, MOCK_EXPERIMENT_ID, "Test-1",
                LocalDateTime.now(), null, null, ExecutionDetails.Status.RUNNING, 1L, 1L, 1L);

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExecutionDTO actualExecutionDTO = subject.createAndStartExecution(executionCreateDTO);

        // Assert
        verify(mockExperimentRepository, times(1)).findById(MOCK_EXPERIMENT_ID);
        verify(mockExecutionRepository, times(2)).save(any(ExecutionDetails.class));
        verify(mockContext, times(3)).getAuthentication();
        assertEquals(mockExecutionDTO.getRam(), actualExecutionDTO.getRam());
        assertEquals(mockExecutionDTO.getCpu(), actualExecutionDTO.getCpu());
        assertEquals(mockExecutionDTO.getName(), actualExecutionDTO.getName());
        assertEquals(mockExecutionDTO.getBookedTime(), actualExecutionDTO.getBookedTime());
        assertEquals(mockExecutionDTO.getStatus(), actualExecutionDTO.getStatus());
        verifyNoMoreInteractions(mockExperimentRepository, mockExecutionRepository, mockContext);
    }

    @Test
    public void testCreateAndStartExecution_validExecutionCreateDTOWithAllOptionalFieldsSet_validExecutionDTOWithSetValues() {

        // Arrange
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.of("Test.zip"), MOCK_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_EXECUTION_ID, MOCK_EXPERIMENT_ID, "Test-1",
                LocalDateTime.now(), null, null, ExecutionDetails.Status.RUNNING, 1L, 1L, 1L);
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "Test", 1L);
        mockExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1", 1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExecutionDTO actualExecutionDTO = subject.createAndStartExecution(executionCreateDTO);

        // Assert
        verify(mockExperimentRepository, times(1)).findById(MOCK_EXPERIMENT_ID);
        verify(mockExecutionRepository, times(2)).save(any(ExecutionDetails.class));
        verify(mockContext, times(3)).getAuthentication();
        assertEquals(mockExecutionDTO.getRam(), actualExecutionDTO.getRam());
        assertEquals(mockExecutionDTO.getCpu(), actualExecutionDTO.getCpu());
        assertEquals(mockExecutionDTO.getName(), actualExecutionDTO.getName());
        assertEquals(mockExecutionDTO.getBookedTime(), actualExecutionDTO.getBookedTime());
        assertEquals(mockExecutionDTO.getStatus(), actualExecutionDTO.getStatus());
        verifyNoMoreInteractions(mockExperimentRepository, mockExecutionRepository, mockContext);
    }

    @Test
    public void testCreateAndStartExecution_invalidIdOfExecutionCreateDTO_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_EXPERIMENT_ID,
                Optional.empty(), Optional.empty(), Optional.empty());
        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.empty());

        // Act
        ExecutionDTO actual = subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_unavailableCluster_throwsException() throws ApiException {

        // Arrange
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("There was an error while communicating with the cluster");
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.of("Test.zip"), MOCK_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, testExperimentDetails, "Test-1",
                1L, 1L, 1L);
        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(Optional.of(testExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);
        when(mockKubernetesClient.createPod(MOCK_USER.getName(), mockExecutionDetails.getExperimentDetails().getName(),
                mockExecutionDetails.getName())).thenThrow(ApiException.class);

        // Act
        ExecutionDTO actual = subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_emptyNames_throwsException() throws ResponseStatusException {

        // Arrange
        UserDTO mockUserDTO = new UserDTO(MOCK_USER_ID, "", "", false);
        JwtAuthentication mockAuthFromNewUserDTO = new JwtAuthentication(mockUserDTO);
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "", 1L);
        mockExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.of("Test.zip"), MOCK_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("Must be at least one alphanumeric letter. Username: " + mockUserDTO.getName() + ", Experimentname: "
                + mockExperimentDetails.getName() + ", Executionname: " + mockExecutionDetails.getName());

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(Optional.of(mockExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(mockAuthFromNewUserDTO);

        // Act
        ExecutionDTO actual = subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_namesDontMatchRegex_throwsException() throws ResponseStatusException {

        // Arrange
        UserDTO mockUserDTO = new UserDTO(MOCK_USER_ID, "Ri88#d", "", false);
        JwtAuthentication mockAuthFromNewUserDTO = new JwtAuthentication(mockUserDTO);
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "+++", 1L);
        mockExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, ".-.",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.of("Test.zip"), MOCK_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("You can only use alphanumeric letters and a hyphen for naming. "
                + "Must start and end alphanumeric. Username: " + mockUserDTO.getName() + ", Experimentname: "
                + mockExperimentDetails.getName() + ", Executionname: " + mockExecutionDetails.getName());

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(Optional.of(mockExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(mockAuthFromNewUserDTO);

        // Act
        ExecutionDTO actual = subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_emptyExecCreateDTONameAndEmptyExperimentName_throwsException() throws ResponseStatusException {

        // Arrange
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "", 1L);
        mockExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "TestExec",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("Experimentname must be at least one alphanumeric letter.");

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(Optional.of(mockExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExecutionDTO actual = subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_emptyExecCreateDTONameAndExperimentNameContainsDot_validExecutionDTO() {

        // Arrange
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "Test.zip", 1L);
        mockExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_EXECUTION_ID, MOCK_EXPERIMENT_ID, "Test-1",
                LocalDateTime.now(), null, null, ExecutionDetails.Status.RUNNING, 1L, 1L, 1L);

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(Optional.of(mockExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExecutionDTO actualExecutionDTO = subject.createAndStartExecution(executionCreateDTO);

        // Assert
        verify(mockExperimentRepository, times(1)).findById(MOCK_EXPERIMENT_ID);
        verify(mockExecutionRepository, times(2)).save(any(ExecutionDetails.class));
        verify(mockContext, times(3)).getAuthentication();
        assertEquals(mockExecutionDTO.getRam(), actualExecutionDTO.getRam());
        assertEquals(mockExecutionDTO.getCpu(), actualExecutionDTO.getCpu());
        assertEquals(mockExecutionDTO.getName(), actualExecutionDTO.getName());
        assertEquals(mockExecutionDTO.getBookedTime(), actualExecutionDTO.getBookedTime());
        assertEquals(mockExecutionDTO.getStatus(), actualExecutionDTO.getStatus());
        verifyNoMoreInteractions(mockExperimentRepository, mockExecutionRepository, mockContext);
    }

    @Test
    public void testCreateAndStartExecution_emptyExecCreateDTONameAndExperimentNameContainsDotAndDoesntMatchRegex_throwsException() throws ResponseStatusException {

        // Arrange
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "Test#.zip", 1L);
        mockExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("You can only use alphanumeric letters and a hyphen for naming. "
                + "Must start and end alphanumeric.");

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(Optional.of(mockExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExecutionDTO actual = subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_emptyExecCreateDTONameAndExperimentNameDoesntMatchRegex_throwsException() throws ResponseStatusException {

        // Arrange
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "+++", 1L);
        mockExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("You can only use alphanumeric letters and a hyphen for naming. "
                + "Must start and end alphanumeric.");

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(Optional.of(mockExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExecutionDTO actual = subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_execCreateDTONameContainsDotAndDoesntMatchRegex_throwsException() throws ResponseStatusException {

        // Arrange
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "Test", 1L);
        mockExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.of("Test#.zip"), MOCK_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("You can only use alphanumeric letters and a hyphen for naming. "
                + "Must start and end alphanumeric.");

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(Optional.of(mockExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExecutionDTO actual = subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_execCreateDTONameMatchesRegex_validExecutionDTO() {

        // Arrange
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "TestExp", 1L);
        mockExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.of("Test"), MOCK_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_EXECUTION_ID, MOCK_EXPERIMENT_ID, "Test-1",
                LocalDateTime.now(), null, null, ExecutionDetails.Status.RUNNING, 1L, 1L, 1L);

        //when(mockExecutionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.of(mockExecutionDetails));
        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(Optional.of(mockExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExecutionDTO actual = subject.createAndStartExecution(executionCreateDTO);

        // Assert
        verify(mockExperimentRepository, times(1)).findById(MOCK_EXPERIMENT_ID);
        verify(mockExecutionRepository, times(2)).save(any(ExecutionDetails.class));
        verify(mockContext, times(3)).getAuthentication();
        assertEquals(mockExecutionDTO.getRam(), actual.getRam());
        assertEquals(mockExecutionDTO.getCpu(), actual.getCpu());
        assertEquals(mockExecutionDTO.getName(), actual.getName());
        assertEquals(mockExecutionDTO.getBookedTime(), actual.getBookedTime());
        assertEquals(mockExecutionDTO.getStatus(), actual.getStatus());
        verifyNoMoreInteractions(mockExperimentRepository, mockExecutionRepository, mockContext);
    }

    @Test
    public void testCreateAndStartExecution_execCreateDTONameDoesntMatchRegex_throwsException() throws ResponseStatusException {

        // Arrange
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "Test", 1L);
        mockExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.of("+++"), MOCK_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("You can only use alphanumeric letters and a hyphen for naming. "
                + "Must start and end alphanumeric.");

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(Optional.of(mockExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExecutionDTO actual = subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testTerminateExecution_validExecutionDTO_validExecutionDTO() {

        // Arrange
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_EXECUTION_ID, MOCK_EXPERIMENT_ID, "Test1",
                LocalDateTime.now(), null, LocalDateTime.now(), ExecutionDetails.Status.CANCELED, 1L, 1L, 1L);
        testExecutionDetails.setPodName("Test1");
        when(mockExecutionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.of(testExecutionDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);

        // Act
        ExecutionDTO actualExecutionDTO = subject.terminateExecution(MOCK_EXECUTION_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findById(MOCK_EXECUTION_ID);
        verify(mockContext, times(2)).getAuthentication();
        assertEquals(mockExecutionDTO.getStatus(), actualExecutionDTO.getStatus());
        assertEquals((mockExecutionDTO.getTerminatedAt().getSecond()), actualExecutionDTO.getTerminatedAt().getSecond());
        verifyNoMoreInteractions(mockContext);
    }

    @Test
    public void testTerminateExecution_invalidExecutionDTO_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        ExecutionDTO executionDTO = new ExecutionDTO(MOCK_EXECUTION_ID, MOCK_EXPERIMENT_ID, "Test1",
                LocalDateTime.now(), null, null, ExecutionDetails.Status.WAITING,
                1L, 1L, 1L);
        when(mockExecutionRepository.findById(executionDTO.getId())).thenReturn(Optional.empty());

        // Act
        ExecutionDTO actual = subject.terminateExecution(MOCK_EXECUTION_ID);

        // Assert - with rule
    }

    @Test
    public void testTerminateExecution_unavailableCluster_throwsException() throws ApiException {

        // Arrange
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("There was an error while communicating with the cluster");
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, testExperimentDetails, "Test1",
                1L, 1L, 1L);
        mockExecutionDetails.setPodName("TestPod");
        when(mockExecutionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.of(mockExecutionDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_AUTH);
        doThrow(new ApiException()).when(mockKubernetesClient)
                .deletePod(MOCK_USER.getName(), mockExecutionDetails.getExperimentDetails().getName(),
                        mockExecutionDetails.getPodName());

        // Act
        ExecutionDTO actual = subject.terminateExecution(MOCK_EXECUTION_ID);

        // Assert - with rule

    }

    @Test
    public void testTerminateExecution_emptyNames_throwsException() throws ResponseStatusException {

        // Arrange
        UserDTO mockUserDTO = new UserDTO(MOCK_USER_ID, "", "", false);
        JwtAuthentication mockAuthFromNewUserDTO = new JwtAuthentication(mockUserDTO);
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "", 1L);
        mockExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("Must be at least one alphanumeric letter. Username: " + mockUserDTO.getName() + ", Experimentname: "
                + mockExperimentDetails.getName() + ", Podname: " + mockExecutionDetails.getPodName());

        when(mockExecutionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.of(mockExecutionDetails));
        when(mockContext.getAuthentication()).thenReturn(mockAuthFromNewUserDTO);

        // Act
        ExecutionDTO actual = subject.terminateExecution(MOCK_EXECUTION_ID);

        // Assert - with rule

    }

    @Test
    public void testTerminateExecution_namesDontMatchRegex_throwsException() throws ResponseStatusException {

        // Arrange
        UserDTO mockUserDTO = new UserDTO(MOCK_USER_ID, "Ri88#d", "", false);
        JwtAuthentication mockAuthFromNewUserDTO = new JwtAuthentication(mockUserDTO);
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "+++", 1L);
        mockExperimentDetails.setId(MOCK_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, ".-.",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_EXECUTION_ID);
        mockExecutionDetails.setPodName(mockExecutionDetails.getName());
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("You can only use alphanumeric letters and a hyphen for naming. "
                + "Must start and end alphanumeric. Username: " + mockUserDTO.getName() + ", Experimentname: "
                + mockExperimentDetails.getName() + ", Podname: " + mockExecutionDetails.getPodName());

        when(mockExecutionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.of(mockExecutionDetails));
        when(mockContext.getAuthentication()).thenReturn(mockAuthFromNewUserDTO);

        // Act
        ExecutionDTO actual = subject.terminateExecution(MOCK_EXECUTION_ID);

        // Assert - with rule

    }
}
