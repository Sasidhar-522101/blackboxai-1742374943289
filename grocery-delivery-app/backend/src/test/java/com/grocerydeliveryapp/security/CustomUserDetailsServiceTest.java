package com.grocerydeliveryapp.security;

import com.grocerydeliveryapp.model.User;
import com.grocerydeliveryapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Set.of("USER"));
        testUser.setEmailVerified(true);
        testUser.setEnabled(true);
    }

    @Test
    void loadUserByUsernameSuccess() {
        // Arrange
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());

        // Assert
        assertNotNull(userDetails);
        assertEquals(testUser.getUsername(), userDetails.getUsername());
        assertEquals(testUser.getPassword(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
    }

    @Test
    void loadUserByUsernameWithEmailSuccess() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getEmail());

        // Assert
        assertNotNull(userDetails);
        assertEquals(testUser.getUsername(), userDetails.getUsername());
        assertEquals(testUser.getPassword(), userDetails.getPassword());
    }

    @Test
    void loadUserByUsernameUserNotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void loadUserByUsernameWithMultipleRoles() {
        // Arrange
        testUser.setRoles(Set.of("USER", "ADMIN"));
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsernameDisabledUser() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());

        // Assert
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsernameUnverifiedEmail() {
        // Arrange
        testUser.setEmailVerified(false);
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());

        // Assert
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsernameLockedAccount() {
        // Arrange
        testUser.setAccountNonLocked(false);
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());

        // Assert
        assertNotNull(userDetails);
        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    void loadUserByUsernameExpiredAccount() {
        // Arrange
        testUser.setAccountNonExpired(false);
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());

        // Assert
        assertNotNull(userDetails);
        assertFalse(userDetails.isAccountNonExpired());
    }

    @Test
    void loadUserByUsernameExpiredCredentials() {
        // Arrange
        testUser.setCredentialsNonExpired(false);
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());

        // Assert
        assertNotNull(userDetails);
        assertFalse(userDetails.isCredentialsNonExpired());
    }

    @Test
    void loadUserByUsernameWithNoRoles() {
        // Arrange
        testUser.setRoles(Set.of());
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().isEmpty());
    }
}
