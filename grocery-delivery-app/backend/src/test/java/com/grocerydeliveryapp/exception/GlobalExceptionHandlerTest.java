package com.grocerydeliveryapp.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationExceptions() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("object", "field1", "error1"));
        fieldErrors.add(new FieldError("object", "field2", "error2"));

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Validation Error", errorResponse.getError());
        assertEquals("Invalid input parameters", errorResponse.getMessage());
        
        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) errorResponse.getDetails();
        assertEquals("error1", details.get("field1"));
        assertEquals("error2", details.get("field2"));
    }

    @Test
    void handleEntityNotFoundException() {
        // Arrange
        String message = "Entity not found";
        EntityNotFoundException ex = new EntityNotFoundException(message);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleEntityNotFoundException(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Not Found", errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
    }

    @Test
    void handleBadCredentialsException() {
        // Arrange
        BadCredentialsException ex = new BadCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentialsException(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Unauthorized", errorResponse.getError());
        assertEquals("Invalid username or password", errorResponse.getMessage());
    }

    @Test
    void handleAccessDeniedException() {
        // Arrange
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(ex);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Forbidden", errorResponse.getError());
        assertEquals("Access denied", errorResponse.getMessage());
    }

    @Test
    void handleOutOfStockException() {
        // Arrange
        OutOfStockException ex = new OutOfStockException("Test Product", 10, 5);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleOutOfStockException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Out of Stock", errorResponse.getError());
        assertTrue(errorResponse.getMessage().contains("Test Product"));
    }

    @Test
    void handleInvalidOtpException() {
        // Arrange
        InvalidOtpException ex = InvalidOtpException.expired("test@example.com");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidOtpException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Invalid OTP", errorResponse.getError());
        assertTrue(errorResponse.getMessage().contains("test@example.com"));
    }

    @Test
    void handlePaymentException() {
        // Arrange
        PaymentException ex = PaymentException.invalidCard("Invalid card number");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handlePaymentException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Payment Error", errorResponse.getError());
        assertEquals("Invalid card number", errorResponse.getMessage());
    }

    @Test
    void handleIllegalStateException() {
        // Arrange
        String message = "Invalid state";
        IllegalStateException ex = new IllegalStateException(message);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalStateException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
    }

    @Test
    void handleAllUncaughtException() {
        // Arrange
        String message = "Unexpected error";
        Exception ex = new Exception(message);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAllUncaughtException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Internal Server Error", errorResponse.getError());
        assertEquals("An unexpected error occurred", errorResponse.getMessage());
        
        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) errorResponse.getDetails();
        assertEquals(message, details.get("error"));
    }
}
