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
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class ExperimentServiceImplTest {

    private static final UUID MOCK_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MOCK_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UserDTO MOCK_USER = new UserDTO(MOCK_USER_ID, "Rick", "", false);
    private static final UserDTO MOCK_ADMIN = new UserDTO(MOCK_ADMIN_ID, "Mortimer", "", true);
    private static final JwtAuthentication MOCK_USER_AUTH = new JwtAuthentication(MOCK_USER);
    private static final JwtAuthentication MOCK_ADMIN_AUTH = new JwtAuthentication(MOCK_ADMIN);

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private ExperimentRepository mockExperimentRepository;

    @Mock
    private SecurityContext mockContext;

    private ExperimentDetails testUserExperimentDetails;

    private ExperimentDetails testAdminExperimentDetails;

    private ExperimentServiceImpl subject;

    @Before
    public void setUp() {
        this.subject = new ExperimentServiceImpl(mockExperimentRepository);
        this.testUserExperimentDetails = new ExperimentDetails(MOCK_USER_ID,
                "testExperiment", "testExperiment,tar", 500);
        this.testAdminExperimentDetails = new ExperimentDetails(MOCK_ADMIN_ID,
                "testAdminExperiment", "testAdminExperiment,tar", 1000);

        SecurityContextHolder.setContext(mockContext);
    }

    @Test
    public void testCreateExperiment_validExperimentDetails_validExperimentDetailsReturned() {

        // Arrange
        when(mockExperimentRepository.save(testUserExperimentDetails)).thenReturn(testUserExperimentDetails);
        when(mockExperimentRepository.findExperimentDetailsByOwnerIdAndName(testUserExperimentDetails.getOwnerId(),
                testUserExperimentDetails.getName())).thenReturn(Collections.emptyList());

        // Act
        ExperimentDetails actual = subject.createExperiment(testUserExperimentDetails);

        // Assert
        verify(mockExperimentRepository, times(1)).save(testUserExperimentDetails);
        verify(mockExperimentRepository, times(1)).findExperimentDetailsByOwnerIdAndName(
                testUserExperimentDetails.getOwnerId(), testUserExperimentDetails.getName());
        assertEquals(testUserExperimentDetails, actual);
        verifyNoMoreInteractions(mockExperimentRepository);
    }

    @Test
    public void testCreateExperiment_validExperimentDetailsExperimentWithNameAlreadyExists_returnsError() {

        // Arrange
        expectedEx.expect(ResponseStatusException.class);
        expectedEx.expectMessage("Experimentname: testExperiment already used. Must be unique.");

        when(mockExperimentRepository.findExperimentDetailsByOwnerIdAndName(testUserExperimentDetails.getOwnerId(),
                testUserExperimentDetails.getName())).thenReturn(Collections.singletonList(testUserExperimentDetails));

        // Act
        ExperimentDetails actual = subject.createExperiment(testUserExperimentDetails);

        // Assert -- with rule
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
        ExperimentDTO mockExperimentDTO = ExperimentDTO.fromExperimentDetails(testUserExperimentDetails);
        when(mockExperimentRepository.findById(MOCK_USER_ID)).thenReturn(Optional.ofNullable(testUserExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        ExperimentDTO actual = subject.retrieveExperimentDTOById(MOCK_USER_ID);

        // Assert
        verify(mockExperimentRepository, times(1)).findById(MOCK_USER_ID);
        verify(mockContext, times(1)).getAuthentication();
        assertEquals(actual, mockExperimentDTO);
        verifyNoMoreInteractions(mockExperimentRepository, mockContext);
    }


    @Test
    public void testRetrieveExperimentDTOById_notOwnerOrAdmin_throwsException() {

        // Arrange
        UserDTO wrongUser = new UserDTO(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Rick", "", false);
        JwtAuthentication auth = new JwtAuthentication(wrongUser);
        when(mockContext.getAuthentication()).thenReturn(auth);

        expectedEx.expect(ResourceNotFoundException.class);
        when(mockExperimentRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(testUserExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(auth);

        // Act
        ExperimentDTO actual = subject.retrieveExperimentDTOById(MOCK_USER_ID);

        // Assert - with rule
    }

    @Test
    public void testRetrieveExperimentDTOById_noExistingId_throwsException() {

        // Arrange
        expectedEx.expect(ResourceNotFoundException.class);
        when(mockExperimentRepository.findById(MOCK_USER_ID)).thenReturn(Optional.empty());

        // Act
        ExperimentDTO actual = subject.retrieveExperimentDTOById(MOCK_USER_ID);

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

    @Test
    public void testRetrieveExperimentsDTOListOfUserId_validId_listReturned() {
        // Arrange
        ExperimentDTO mockExperimentDTO = ExperimentDTO.fromExperimentDetails(testUserExperimentDetails);
        when(mockExperimentRepository.findExperimentDetailsByOwnerIdOrPermittedAccountsContaining(MOCK_USER_ID, MOCK_USER_ID)).thenReturn(
                Collections.singletonList(testUserExperimentDetails));
        when(mockContext.getAuthentication()).thenReturn(MOCK_USER_AUTH);

        // Act
        List<ExperimentDTO> actual = subject.retrieveExperimentsDTOListOfUserId(MOCK_USER_ID);

        // Assert
        verify(mockExperimentRepository, times(1)).findExperimentDetailsByOwnerIdOrPermittedAccountsContaining(MOCK_USER_ID, MOCK_USER_ID);
        verify(mockContext, times(1)).getAuthentication();
        assertThat(actual, containsInAnyOrder(mockExperimentDTO));
        verifyNoMoreInteractions(mockExperimentRepository, mockContext);
    }

    @Test
    public void testRetrieveAllExperimentsDTOList_isAdmin_listReturned() {
        // Arrange
        ExperimentDTO mockUserExperimentDTO = ExperimentDTO.fromExperimentDetails(testUserExperimentDetails);
        ExperimentDTO mockAdminExperimentDTO = ExperimentDTO.fromExperimentDetails(testAdminExperimentDetails);
        List<ExperimentDetails> list = Arrays.asList(testUserExperimentDetails, testAdminExperimentDetails);
        when(mockExperimentRepository.findAll()).thenReturn(list);
        when(mockContext.getAuthentication()).thenReturn(MOCK_ADMIN_AUTH);

        // Act
        List<ExperimentDTO> actual = subject.retrieveAllExperimentDTOs();

        // Assert
        verify(mockExperimentRepository, times(1)).findAll();
        verify(mockContext, times(2)).getAuthentication();
        assertThat(actual, containsInAnyOrder(mockUserExperimentDTO, mockAdminExperimentDTO));
        verifyNoMoreInteractions(mockExperimentRepository, mockContext);
    }
}
