package edu.tcu.projectpulse.service;

import edu.tcu.projectpulse.domain.Role;
import edu.tcu.projectpulse.domain.UserAccount;
import edu.tcu.projectpulse.dto.ProfileUpdateRequest;
import edu.tcu.projectpulse.dto.ProfileUpdateResponse;
import edu.tcu.projectpulse.repo.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectPulseServiceProfileTest {

    @Mock
    private UserAccountRepository userRepo;

    @InjectMocks
    private ProjectPulseService service;

    private UserAccount testStudent;

    @BeforeEach
    void setUp() {
        testStudent = new UserAccount();
        testStudent.setId(1L);
        testStudent.setFirstName("Sam");
        testStudent.setLastName("Student");
        testStudent.setEmail("student1@projectpulse.local");
        testStudent.setRole(Role.STUDENT);
        testStudent.setActive(true);
    }

    @Test
    void testUpdateStudentProfile_Success() {
        // Arrange
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@test.com");

        when(userRepo.findById(1L)).thenReturn(Optional.of(testStudent));
        when(userRepo.findByEmailIgnoreCase("john.doe@test.com")).thenReturn(Optional.empty());
        when(userRepo.save(any(UserAccount.class))).thenReturn(testStudent);

        // Act
        ProfileUpdateResponse response = service.updateStudentProfile(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john.doe@test.com", response.getEmail());
        assertEquals("Profile updated successfully", response.getMessage());

        verify(userRepo).findById(1L);
        verify(userRepo).findByEmailIgnoreCase("john.doe@test.com");
        verify(userRepo).save(testStudent);
    }

    @Test
    void testUpdateStudentProfile_EmailAlreadyExists() {
        // Arrange
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("existing.email@test.com");

        UserAccount existingUser = new UserAccount();
        existingUser.setId(2L);
        existingUser.setEmail("existing.email@test.com");

        when(userRepo.findById(1L)).thenReturn(Optional.of(testStudent));
        when(userRepo.findByEmailIgnoreCase("existing.email@test.com")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.updateStudentProfile(1L, request);
        });

        verify(userRepo).findById(1L);
        verify(userRepo).findByEmailIgnoreCase("existing.email@test.com");
        verify(userRepo, never()).save(any());
    }

    @Test
    void testUpdateStudentProfile_NonStudentUser() {
        // Arrange
        testStudent.setRole(Role.INSTRUCTOR);
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@test.com");

        when(userRepo.findById(1L)).thenReturn(Optional.of(testStudent));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.updateStudentProfile(1L, request);
        });

        verify(userRepo).findById(1L);
        verify(userRepo, never()).save(any());
    }

    @Test
    void testUpdateStudentProfile_UserNotFound() {
        // Arrange
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@test.com");

        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.updateStudentProfile(1L, request);
        });

        verify(userRepo).findById(1L);
        verify(userRepo, never()).save(any());
    }
}
