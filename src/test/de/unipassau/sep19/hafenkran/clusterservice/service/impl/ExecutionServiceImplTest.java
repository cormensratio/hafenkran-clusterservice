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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionServiceImplTest {

    private static final UUID MOCK_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MOCK_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UserDTO MOCK_USER = new UserDTO(MOCK_USER_ID, "Rick", "", false);
    private static final UserDTO MOCK_ADMIN = new UserDTO(MOCK_ADMIN_ID, "Mortimer", "", true);
    private static final JwtAuthentication MOCK_USER_AUTH = new JwtAuthentication(MOCK_USER);
    private static final JwtAuthentication MOCK_ADMIN_AUTH = new JwtAuthentication(MOCK_ADMIN);
    private static final UUID MOCK_USER_EXPERIMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MOCK_USER_EXECUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MOCK_ADMIN_EXPERIMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID MOCK_ADMIN_EXECUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");


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

    private ExperimentDetails testUserExperimentDetails;

    private ExecutionDetails testUserExecutionDetails;

    private ExperimentDetails testAdminExperimentDetails;

    private ExecutionDetails testAdminExecutionDetails;

    private List<ExecutionDetails> testExecutionDetailsList;

    private List<ExecutionDTO> testExecutionDTOS;

    private ExecutionServiceImpl subject;

    @Before
    public void setUp() {
        SecurityContextHolder.setContext(mockContext);

        this.subject = new ExecutionServiceImpl(mockExecutionRepository, mockExperimentRepository,
                mockKubernetesClient);

        ExperimentDetails experimentDetails =
                new ExperimentDetails(MOCK_USER_ID, "testExperiment",
                        "testExperiment.tar", 500);
        experimentDetails.setId(MOCK_USER_EXPERIMENT_ID);

        this.testUserExperimentDetails = new ExperimentDetails(MOCK_USER_ID,
                "ExpTest", "ExpTest.tar", 1L);
        testUserExperimentDetails.setId(MOCK_USER_EXPERIMENT_ID);
        this.testUserExecutionDetails = new ExecutionDetails(MOCK_USER_ID, experimentDetails, "Test1", 1L, 1L, 1L);
        testUserExecutionDetails.setId(MOCK_USER_EXECUTION_ID);

        this.testAdminExperimentDetails = new ExperimentDetails(MOCK_ADMIN_ID,
                "ExpTest", "ExpTest.tar", 1L);
        testAdminExperimentDetails.setId(MOCK_ADMIN_EXPERIMENT_ID);
        this.testAdminExecutionDetails = new ExecutionDetails(MOCK_ADMIN_ID, experimentDetails, "Test1", 1L, 1L, 1L);
        testAdminExecutionDetails.setId(MOCK_ADMIN_EXECUTION_ID);

        this.testExecutionDetailsList = new ArrayList<>();
        this.testExecutionDTOS = new ArrayList<>();
    }

    @Test
    public void testRetrieveExecutionDTOById_existingId_validExecutionDTO() {

        // Arrange
        ExecutionDTO mockExecutionDTO = ExecutionDTO.fromExecutionDetails(testUserExecutionDetails);
        when(mockExecutionRepository.findById(MOCK_USER_EXECUTION_ID)).thenReturn(Optional.ofNullable(testUserExecutionDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        ExecutionDTO actual = subject.retrieveExecutionDTOById(MOCK_USER_EXECUTION_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findById(MOCK_USER_EXECUTION_ID);
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

        when(mockExecutionRepository.findById(MOCK_USER_EXECUTION_ID)).thenReturn(Optional.ofNullable(testUserExecutionDetails));
        when(mockContext.getAuthentication()).thenReturn(notOwnerAuth);

        // Act
        subject.retrieveExecutionDTOById(MOCK_USER_EXECUTION_ID);

        // Assert -- with rule
    }

    @Test
    public void testRetrieveExecutionDTOById_noExistingId_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        when(mockExecutionRepository.findById(MOCK_USER_EXECUTION_ID)).thenReturn(Optional.empty());

        // Act
        subject.retrieveExecutionDTOById(MOCK_USER_EXECUTION_ID);

        // Assert - with rule

    }

    @Test
    public void testRetrieveExecutionDTOById_idIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("id is marked non-null but is null");

        // Act
        subject.retrieveExecutionDTOById(null);

        // Assert - with rule

    }

    @Test
    public void testRetrieveExecutionsDTOListOfExperimentId_existingId_validExecutionDTOList() {

        // Arrange
        testExecutionDTOS.add(new ExecutionDTO(testUserExecutionDetails.getId(),
                testUserExecutionDetails.getExperimentDetails().getId(), testUserExecutionDetails.getName(),
                testUserExecutionDetails.getCreatedAt(),
                testUserExecutionDetails.getStartedAt(), testUserExecutionDetails.getTerminatedAt(),
                testUserExecutionDetails.getStatus(), testUserExecutionDetails.getRam(), testUserExecutionDetails.getCpu(),
                testUserExecutionDetails.getBookedTime()));
        testExecutionDetailsList.add(testUserExecutionDetails);
        when(mockExecutionRepository.findAllByExperimentDetails_Id(MOCK_USER_EXPERIMENT_ID)).thenReturn(
                testExecutionDetailsList);
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        List<ExecutionDTO> actual = subject.retrieveExecutionsDTOListOfExperimentId(MOCK_USER_EXPERIMENT_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findAllByExperimentDetails_Id(MOCK_USER_EXPERIMENT_ID);
        verify(mockContext, times(1)).getAuthentication();
        assertEquals(testExecutionDTOS, actual);
        verifyNoMoreInteractions(mockExecutionRepository, mockContext);
    }

    @Test
    public void testRetrieveExecutionsDTOListOfExperimentId_noExistingId_emptyExecutionDetailsList() {

        // Arrange
        when(mockExecutionRepository.findAllByExperimentDetails_Id(MOCK_USER_EXPERIMENT_ID)).thenReturn(
                testExecutionDetailsList);

        // Act
        List<ExecutionDTO> actual = subject.retrieveExecutionsDTOListOfExperimentId(MOCK_USER_EXPERIMENT_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findAllByExperimentDetails_Id(MOCK_USER_EXPERIMENT_ID);
        assertEquals(testExecutionDTOS, actual);
        verifyNoMoreInteractions(mockExecutionRepository);
    }

    @Test
    public void testRetrieveExecutionsDTOListOfExperimentId_experimentIdIsNull_throwsException() {

        // Arrange
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("experimentId is marked non-null but is null");

        // Act
        subject.retrieveExecutionsDTOListOfExperimentId(null);

        // Assert - with rule

    }

    @Test
    public void testRetrieveExecutionsDTOListOfExperimentId_validIdButEmptyExecutionList_emptyExecutionDetailsList() {

        // Arrange
        when(mockExecutionRepository.findAllByExperimentDetails_Id(MOCK_USER_EXPERIMENT_ID)).thenReturn(
                Collections.emptyList());

        // Act
        List<ExecutionDTO> actual = subject.retrieveExecutionsDTOListOfExperimentId(MOCK_USER_EXPERIMENT_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findAllByExperimentDetails_Id(MOCK_USER_EXPERIMENT_ID);
        assertEquals(testExecutionDTOS, actual);
        verifyNoMoreInteractions(mockExecutionRepository);
    }

    @Test
    public void testRetrieveExecutionsDTOListForUserId_existingUserId_validExecutionDTOList() {

        // Arrange
        testExecutionDTOS.add(new ExecutionDTO(testUserExecutionDetails.getId(),
                testUserExecutionDetails.getExperimentDetails().getId(), testUserExecutionDetails.getName(),
                testUserExecutionDetails.getCreatedAt(),
                testUserExecutionDetails.getStartedAt(), testUserExecutionDetails.getTerminatedAt(),
                testUserExecutionDetails.getStatus(), testUserExecutionDetails.getRam(), testUserExecutionDetails.getCpu(),
                testUserExecutionDetails.getBookedTime()));
        testExecutionDetailsList.add(testUserExecutionDetails);
        when(mockExecutionRepository.findAllByExperimentDetails_OwnerId(MOCK_USER_ID)).thenReturn(
                testExecutionDetailsList);
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        List<ExecutionDTO> actual = subject.retrieveExecutionsDTOListForUserId(MOCK_USER_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findAllByExperimentDetails_OwnerId(MOCK_USER_ID);
        verify(mockContext, times(1)).getAuthentication();
        assertEquals(testExecutionDTOS, actual);
        verifyNoMoreInteractions(mockExecutionRepository, mockContext);
    }

    @Test
    public void testRetrieveAllExecutionDTOList_isAdmin_validExecutionDTOList() {

        // Arrange
        ExecutionDTO mockUserExecution = ExecutionDTO.fromExecutionDetails(testUserExecutionDetails);
        ExecutionDTO mockAdminExecution = ExecutionDTO.fromExecutionDetails(testAdminExecutionDetails);
        Iterable<ExecutionDetails> list = Arrays.asList(testUserExecutionDetails, testAdminExecutionDetails);
        when(mockExecutionRepository.findAll()).thenReturn(list);
        when(mockContext.getAuthentication()).thenReturn(MOCK_ADMIN_AUTH);

        // Act
        List<ExecutionDTO> actual = subject.retrieveAllExecutionsDTOs();

        // Assert
        verify(mockExecutionRepository, times(1)).findAll();
        verify(mockContext, times(2)).getAuthentication();
        assertThat(actual, containsInAnyOrder(mockUserExecution, mockAdminExecution));
        verifyNoMoreInteractions(mockExecutionRepository, mockContext);
    }

    @Test
    public void testRetrieveExecutionsDTOListForUserId_noExecutionForUser_returnsEmptyList() {

        // Arrange


        // Act
        subject.retrieveExecutionsDTOListForUserId(MOCK_USER_ID);

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
        subject.retrieveExecutionsDTOListForUserId(null);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_validExecutionCreateDTOWithAllOptionalFieldsEmpty_validExecutionDTOWithDefaultValues() {

        // Arrange
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_USER_EXPERIMENT_ID,
                Optional.empty(), Optional.empty(), Optional.empty());
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "Test", "filename", 1L);
        mockExperimentDetails.setId(MOCK_USER_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1", 1L,
                1L, 1L);
        mockExecutionDetails.setId(MOCK_USER_EXECUTION_ID);
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_USER_EXECUTION_ID, MOCK_USER_EXPERIMENT_ID, "Test-1",
                LocalDateTime.now(), null, null, ExecutionDetails.Status.RUNNING, 1L, 1L, 1L);

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        ExecutionDTO actualExecutionDTO = subject.createAndStartExecution(executionCreateDTO);

        // Assert
        verify(mockExperimentRepository, times(1)).findById(MOCK_USER_EXPERIMENT_ID);
        verify(mockExecutionRepository, times(2)).save(any(ExecutionDetails.class));
        verify(mockContext, times(2)).getAuthentication();
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
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.of("Test.zip"), MOCK_USER_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_USER_EXECUTION_ID, MOCK_USER_EXPERIMENT_ID, "Test-1",
                LocalDateTime.now(), null, null, ExecutionDetails.Status.RUNNING, 1L, 1L, 1L);
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "Test", "filename", 1L);
        mockExperimentDetails.setId(MOCK_USER_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1", 1L,
                1L, 1L);
        mockExecutionDetails.setId(MOCK_USER_EXECUTION_ID);

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        ExecutionDTO actualExecutionDTO = subject.createAndStartExecution(executionCreateDTO);

        // Assert
        verify(mockExperimentRepository, times(1)).findById(MOCK_USER_EXPERIMENT_ID);
        verify(mockExecutionRepository, times(2)).save(any(ExecutionDetails.class));
        verify(mockContext, times(2)).getAuthentication();
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
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_USER_EXPERIMENT_ID,
                Optional.empty(), Optional.empty(), Optional.empty());
        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.empty());

        // Act
        subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_unavailableCluster_throwsException() throws ApiException {

        // Arrange
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("There was an error while communicating with the cluster");
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.of("Test.zip"), MOCK_USER_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, testUserExperimentDetails, "Test-1",
                1L, 1L, 1L);
        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.of(testUserExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);
        when(mockKubernetesClient.createPod(mockExecutionDetails)).thenThrow(ApiException.class);

        // Act
        subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_emptyExecCreateDTONameAndEmptyExperimentName_throwsException() throws ResponseStatusException {

        // Arrange
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "", "filename", 1L);
        mockExperimentDetails.setId(MOCK_USER_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "TestExec",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_USER_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_USER_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("Experimentname must be at least one alphanumeric letter.");

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_emptyExecCreateDTONameAndExperimentNameContainsDot_validExecutionDTO() {

        // Arrange
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "Test", "filename", 1L);
        mockExperimentDetails.setId(MOCK_USER_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_USER_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_USER_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_USER_EXECUTION_ID, MOCK_USER_EXPERIMENT_ID, "Test-1",
                LocalDateTime.now(), null, null, ExecutionDetails.Status.RUNNING, 1L, 1L, 1L);

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        ExecutionDTO actualExecutionDTO = subject.createAndStartExecution(executionCreateDTO);

        // Assert
        verify(mockExperimentRepository, times(1)).findById(MOCK_USER_EXPERIMENT_ID);
        verify(mockExecutionRepository, times(2)).save(any(ExecutionDetails.class));
        verify(mockContext, times(2)).getAuthentication();
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
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "Test#.zip", "filename", 1L);
        mockExperimentDetails.setId(MOCK_USER_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_USER_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_USER_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("You can only use alphanumeric letters and a hyphen for naming. "
                + "Must start and end alphanumeric.");

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_emptyExecCreateDTONameAndExperimentNameDoesntMatchRegex_throwsException() throws ResponseStatusException {

        // Arrange
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "+++", "filename", 1L);
        mockExperimentDetails.setId(MOCK_USER_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_USER_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.empty(), MOCK_USER_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("You can only use alphanumeric letters and a hyphen for naming. "
                + "Must start and end alphanumeric.");

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_execCreateDTONameContainsDotAndDoesntMatchRegex_throwsException() throws ResponseStatusException {

        // Arrange
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "Test", "filename", 1L);
        mockExperimentDetails.setId(MOCK_USER_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_USER_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.of("Test#.zip"), MOCK_USER_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("You can only use alphanumeric letters and a hyphen for naming. "
                + "Must start and end alphanumeric.");

        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        subject.createAndStartExecution(executionCreateDTO);

        // Assert - with rule

    }

    @Test
    public void testCreateAndStartExecution_execCreateDTONameMatchesRegex_validExecutionDTO() {

        // Arrange
        ExperimentDetails mockExperimentDetails = new ExperimentDetails(MOCK_USER_ID, "TestExp", "filename", 1L);
        mockExperimentDetails.setId(MOCK_USER_EXPERIMENT_ID);
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, mockExperimentDetails, "Test-1",
                1L, 1L, 1L);
        mockExecutionDetails.setId(MOCK_USER_EXECUTION_ID);
        ExecutionCreateDTO executionCreateDTO = new ExecutionCreateDTO(Optional.of("Test"), MOCK_USER_EXPERIMENT_ID,
                Optional.of(1L), Optional.of(1L), Optional.of(1L));
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_USER_EXECUTION_ID, MOCK_USER_EXPERIMENT_ID, "Test-1",
                LocalDateTime.now(), null, null, ExecutionDetails.Status.RUNNING, 1L, 1L, 1L);

        //when(mockExecutionRepository.findById(MOCK_EXECUTION_ID)).thenReturn(Optional.of(mockExecutionDetails));
        when(mockExperimentRepository.findById(executionCreateDTO.getExperimentId())).thenReturn(
                Optional.of(mockExperimentDetails));
        when(mockExecutionRepository.save(any(ExecutionDetails.class))).thenReturn(mockExecutionDetails);
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        ExecutionDTO actual = subject.createAndStartExecution(executionCreateDTO);

        // Assert
        verify(mockExperimentRepository, times(1)).findById(MOCK_USER_EXPERIMENT_ID);
        verify(mockExecutionRepository, times(2)).save(any(ExecutionDetails.class));
        verify(mockContext, times(2)).getAuthentication();
        assertEquals(mockExecutionDTO.getRam(), actual.getRam());
        assertEquals(mockExecutionDTO.getCpu(), actual.getCpu());
        assertEquals(mockExecutionDTO.getName(), actual.getName());
        assertEquals(mockExecutionDTO.getBookedTime(), actual.getBookedTime());
        assertEquals(mockExecutionDTO.getStatus(), actual.getStatus());
        verifyNoMoreInteractions(mockExperimentRepository, mockExecutionRepository, mockContext);
    }

    @Test
    public void testTerminateExecution_validExecutionDTO_validExecutionDTO() {

        // Arrange
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_USER_EXECUTION_ID, MOCK_USER_EXPERIMENT_ID, "Test1",
                LocalDateTime.now(), null, LocalDateTime.now(), ExecutionDetails.Status.CANCELED, 1L, 1L, 1L);
        testUserExecutionDetails.setPodName("Test1");
        when(mockExecutionRepository.findById(MOCK_USER_EXECUTION_ID)).thenReturn(Optional.of(testUserExecutionDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        ExecutionDTO actualExecutionDTO = subject.terminateExecution(MOCK_USER_EXECUTION_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findById(MOCK_USER_EXECUTION_ID);
        verify(mockContext, times(2)).getAuthentication();
        assertEquals(mockExecutionDTO.getStatus(), actualExecutionDTO.getStatus());
        assertEquals((mockExecutionDTO.getTerminatedAt().getSecond()),
                actualExecutionDTO.getTerminatedAt().getSecond());
        verifyNoMoreInteractions(mockContext);
    }

    @Test
    public void testTerminateExecution_AdminTerminatesExecutionAdminIsOwner_validExecutionDTO() {

        // Arrange
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_ADMIN_EXECUTION_ID, MOCK_ADMIN_EXPERIMENT_ID, "Test1",
                LocalDateTime.now(), null, LocalDateTime.now(), ExecutionDetails.Status.CANCELED, 1L, 1L, 1L);
        testAdminExecutionDetails.setPodName("Test1");
        when(mockExecutionRepository.findById(MOCK_ADMIN_EXECUTION_ID)).thenReturn(Optional.of(testAdminExecutionDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_ADMIN_AUTH);

        // Act
        ExecutionDTO actualExecutionDTO = subject.terminateExecution(MOCK_ADMIN_EXECUTION_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findById(MOCK_ADMIN_EXECUTION_ID);
        verify(mockContext, times(2)).getAuthentication();
        assertEquals(mockExecutionDTO.getStatus(), actualExecutionDTO.getStatus());
        assertEquals((mockExecutionDTO.getTerminatedAt().getSecond()),
                actualExecutionDTO.getTerminatedAt().getSecond());
        verifyNoMoreInteractions(mockContext);
    }

    @Test
    public void testTerminateExecution_AdminTerminatesExecutionAdminIsNotOwner_validExecutionDTO() {

        // Arrange
        ExecutionDTO mockExecutionDTO = new ExecutionDTO(MOCK_USER_EXECUTION_ID, MOCK_USER_EXPERIMENT_ID, "Test1",
                LocalDateTime.now(), null, LocalDateTime.now(), ExecutionDetails.Status.ABORTED, 1L, 1L, 1L);
        testUserExecutionDetails.setPodName("Test1");
        when(mockExecutionRepository.findById(MOCK_USER_EXECUTION_ID)).thenReturn(Optional.of(testUserExecutionDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_ADMIN_AUTH);

        // Act
        ExecutionDTO actualExecutionDTO = subject.terminateExecution(MOCK_USER_EXECUTION_ID);

        // Assert
        verify(mockExecutionRepository, times(1)).findById(MOCK_USER_EXECUTION_ID);
        verify(mockContext, times(2)).getAuthentication();
        assertEquals(mockExecutionDTO.getStatus(), actualExecutionDTO.getStatus());
        assertEquals((mockExecutionDTO.getTerminatedAt().getSecond()),
                actualExecutionDTO.getTerminatedAt().getSecond());
        verifyNoMoreInteractions(mockContext);
    }

    @Test
    public void testTerminateExecution_invalidExecutionDTO_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        ExecutionDTO executionDTO = new ExecutionDTO(MOCK_USER_EXECUTION_ID, MOCK_USER_EXPERIMENT_ID, "Test1",
                LocalDateTime.now(), null, null, ExecutionDetails.Status.WAITING,
                1L, 1L, 1L);
        when(mockExecutionRepository.findById(executionDTO.getId())).thenReturn(Optional.empty());

        // Act
        subject.terminateExecution(MOCK_USER_EXECUTION_ID);

        // Assert - with rule
    }

    @Test
    public void testTerminateExecution_unavailableCluster_throwsException() throws ApiException {

        // Arrange
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("There was an error while communicating with the cluster");
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, testUserExperimentDetails, "Test1",
                1L, 1L, 1L);
        mockExecutionDetails.setPodName("TestPod");
        when(mockExecutionRepository.findById(MOCK_USER_EXECUTION_ID)).thenReturn(Optional.of(mockExecutionDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);
        doThrow(new ApiException()).when(mockKubernetesClient)
                .deletePod(mockExecutionDetails);

        // Act
        subject.terminateExecution(MOCK_USER_EXECUTION_ID);

        // Assert - with rule

    }

    @Test
    public void testRetrieveLogsForExecutionId_validInput_validLogReturned() throws ApiException {
        // Arrange
        int lines = 5;
        int sinceSeconds = 5;
        String expectedLog = "Test Log";
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, testUserExperimentDetails, "Test1",
                1L, 1L, 1L);
        when(mockExecutionRepository.findById(MOCK_USER_EXECUTION_ID)).thenReturn(Optional.of(mockExecutionDetails));
        when(mockKubernetesClient.retrieveLogs(mockExecutionDetails, lines, sinceSeconds,
                true)).thenReturn(
                expectedLog);
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        String actual = subject.retrieveLogsForExecutionId(MOCK_USER_EXECUTION_ID, 5, 5, true);

        // Assert
        assertEquals(expectedLog, actual);
        verify(mockExecutionRepository, times(1)).findById(MOCK_USER_EXECUTION_ID);
        verify(mockKubernetesClient, times(1)).retrieveLogs(mockExecutionDetails, lines,
                sinceSeconds, true);
        verify(mockContext, times(1)).getAuthentication();
    }

    @Test
    public void testRetrieveLogsForExecutionId_noConnectionToKubernetes_throwsException() throws ApiException {
        // Arrange
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("There was an error while communicating with the cluster");

        int lines = 5;
        int sinceSeconds = 5;
        ExecutionDetails mockExecutionDetails = new ExecutionDetails(MOCK_USER_ID, testUserExperimentDetails, "Test1",
                1L, 1L, 1L);
        when(mockExecutionRepository.findById(MOCK_USER_EXECUTION_ID)).thenReturn(Optional.of(mockExecutionDetails));
        when(mockKubernetesClient.retrieveLogs(mockExecutionDetails, lines, sinceSeconds,
                true)).thenThrow(
                ApiException.class);
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        subject.retrieveLogsForExecutionId(MOCK_USER_EXECUTION_ID, 5, 5, true);

        // Assert -- with logs
    }
}
