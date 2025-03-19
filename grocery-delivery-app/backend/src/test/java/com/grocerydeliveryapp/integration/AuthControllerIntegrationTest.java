package com.grocerydeliveryapp.integration;

import com.grocerydeliveryapp.dto.auth.LoginRequest;
import com.grocerydeliveryapp.dto.auth.RegisterRequest;
import com.grocerydeliveryapp.dto.auth.AuthResponse;
import com.grocerydeliveryapp.dto.auth.OtpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

public class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void registerUserSuccess() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("Password@123");
        request.setPhoneNumber("+1234567890");
        request.setAddress("456 New St");

        // Act
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        AuthResponse response = fromJson(result.getResponse().getContentAsString(), AuthResponse.class);
        assertNotNull(response);
        assertTrue(response.getMessage().contains("Registration successful"));
    }

    @Test
    void registerUserDuplicateUsername() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser"); // Existing username
        request.setEmail("another@example.com");
        request.setPassword("Password@123");
        request.setPhoneNumber("+1234567890");
        request.setAddress("456 New St");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginSuccess() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("Test@123");

        // Act
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        AuthResponse response = fromJson(result.getResponse().getContentAsString(), AuthResponse.class);
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("testuser", response.getUsername());
        assertTrue(response.isEmailVerified());
    }

    @Test
    void loginFailureInvalidCredentials() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyOtpSuccess() throws Exception {
        // Arrange
        OtpRequest request = new OtpRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456"); // This should match the OTP in the database

        // Act
        MvcResult result = mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        AuthResponse response = fromJson(result.getResponse().getContentAsString(), AuthResponse.class);
        assertNotNull(response);
        assertNotNull(response.getToken());
    }

    @Test
    void resendOtpSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(post("/api/auth/resend-otp")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        AuthResponse response = fromJson(result.getResponse().getContentAsString(), AuthResponse.class);
        assertNotNull(response);
        assertTrue(response.getMessage().contains("New OTP has been sent"));
    }

    @Test
    void checkEmailAvailability() throws Exception {
        // Act & Assert - Existing email
        mockMvc.perform(get("/api/auth/check-email")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(result -> 
                    assertEquals("false", result.getResponse().getContentAsString()));

        // Act & Assert - New email
        mockMvc.perform(get("/api/auth/check-email")
                .param("email", "new@example.com"))
                .andExpect(status().isOk())
                .andExpect(result -> 
                    assertEquals("true", result.getResponse().getContentAsString()));
    }

    @Test
    void checkUsernameAvailability() throws Exception {
        // Act & Assert - Existing username
        mockMvc.perform(get("/api/auth/check-username")
                .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(result -> 
                    assertEquals("false", result.getResponse().getContentAsString()));

        // Act & Assert - New username
        mockMvc.perform(get("/api/auth/check-username")
                .param("username", "newuser"))
                .andExpect(status().isOk())
                .andExpect(result -> 
                    assertEquals("true", result.getResponse().getContentAsString()));
    }
}
