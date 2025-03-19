package com.grocerydeliveryapp.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class WebSecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testPublicEndpointsAccessible() throws Exception {
        // Test access to public endpoints
        mockMvc.perform(get("/api/auth/check-username")
                .param("username", "testuser"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/check-email")
                .param("email", "test@example.com"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointsUnauthorized() throws Exception {
        // Test access to protected endpoints without authentication
        mockMvc.perform(post("/api/products"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/products/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAdminEndpointsUnauthorized() throws Exception {
        // Test access to admin endpoints without authentication
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/admin/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCorsConfiguration() throws Exception {
        mockMvc.perform(options("/api/products")
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"));
    }

    @Test
    void testPasswordEncoder() {
        // Test password encoder configuration
        String rawPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testAuthenticationManagerConfiguration(@Autowired AuthenticationManager authenticationManager) {
        // Arrange
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password(passwordEncoder.encode("password"))
                .authorities("ROLE_USER")
                .build();

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // Act & Assert
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("testuser", "password")
        );

        assertTrue(authentication.isAuthenticated());
        assertEquals("testuser", authentication.getName());
    }

    @Test
    void testInvalidAuthentication(@Autowired AuthenticationManager authenticationManager) {
        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken("testuser", "wrongpassword")
            );
        });
    }

    @Test
    void testWebSocketEndpointsAccessible() throws Exception {
        mockMvc.perform(get("/ws"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/ws/**"))
                .andExpect(status().isOk());
    }

    @Test
    void testSwaggerEndpointsAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void testH2ConsoleAccessible() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isOk());
    }

    @Test
    void testActuatorEndpointsAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }
}
