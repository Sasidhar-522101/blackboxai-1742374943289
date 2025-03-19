package com.grocerydeliveryapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;
    private final String SECRET_KEY = "testSecretKey2023ForTestingPurposesOnly";
    private final long EXPIRATION_TIME = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET_KEY, EXPIRATION_TIME);
        userDetails = User.withUsername("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    void generateTokenSuccess() {
        // Act
        String token = jwtUtil.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void generateTokenWithCustomClaimsSuccess() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com");
        claims.put("userId", "123");

        // Act
        String token = jwtUtil.generateToken(claims, userDetails);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Verify claims
        Claims extractedClaims = jwtUtil.extractAllClaims(token);
        assertEquals("test@example.com", extractedClaims.get("email"));
        assertEquals("123", extractedClaims.get("userId"));
    }

    @Test
    void validateTokenSuccess() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);

        // Act
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateTokenFailureExpired() {
        // Arrange
        JwtUtil shortExpirationJwtUtil = new JwtUtil(SECRET_KEY, 0); // Immediate expiration
        String token = shortExpirationJwtUtil.generateToken(userDetails);

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> 
            shortExpirationJwtUtil.validateToken(token, userDetails)
        );
    }

    @Test
    void validateTokenFailureInvalidSignature() {
        // Arrange
        JwtUtil differentSecretJwtUtil = new JwtUtil("differentSecret", EXPIRATION_TIME);
        String token = differentSecretJwtUtil.generateToken(userDetails);

        // Act & Assert
        assertThrows(SignatureException.class, () -> 
            jwtUtil.validateToken(token, userDetails)
        );
    }

    @Test
    void validateTokenFailureWrongUser() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);
        UserDetails wrongUser = User.withUsername("wronguser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        // Act
        boolean isValid = jwtUtil.validateToken(token, wrongUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void extractUsernameSuccess() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void extractExpirationSuccess() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);

        // Act
        Date expiration = jwtUtil.extractExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void extractClaimSuccess() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("customClaim", "customValue");
        String token = jwtUtil.generateToken(claims, userDetails);

        // Act
        String customClaim = jwtUtil.extractClaim(token, claims -> claims.get("customClaim", String.class));

        // Assert
        assertEquals("customValue", customClaim);
    }

    @Test
    void extractAllClaimsSuccess() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com");
        claims.put("userId", "123");
        String token = jwtUtil.generateToken(claims, userDetails);

        // Act
        Claims extractedClaims = jwtUtil.extractAllClaims(token);

        // Assert
        assertNotNull(extractedClaims);
        assertEquals("test@example.com", extractedClaims.get("email"));
        assertEquals("123", extractedClaims.get("userId"));
        assertEquals("testuser", extractedClaims.getSubject());
    }

    @Test
    void isTokenExpiredSuccess() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);

        // Act
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    void isTokenExpiredTrue() {
        // Arrange
        JwtUtil shortExpirationJwtUtil = new JwtUtil(SECRET_KEY, 0); // Immediate expiration
        String token = shortExpirationJwtUtil.generateToken(userDetails);

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> 
            shortExpirationJwtUtil.isTokenExpired(token)
        );
    }

    @Test
    void createTokenWithCustomExpirationSuccess() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("customClaim", "value");
        String subject = "testuser";
        long customExpiration = 7200000; // 2 hours

        // Act
        String token = jwtUtil.createToken(claims, subject, customExpiration);

        // Assert
        assertNotNull(token);
        Claims extractedClaims = jwtUtil.extractAllClaims(token);
        assertEquals("value", extractedClaims.get("customClaim"));
        assertEquals(subject, extractedClaims.getSubject());
        assertTrue(extractedClaims.getExpiration().after(new Date()));
    }
}
