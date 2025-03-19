package com.grocerydeliveryapp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grocerydeliveryapp.config.TestConfig;
import com.grocerydeliveryapp.model.User;
import com.grocerydeliveryapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected String testJwtToken;

    @Autowired
    protected String testAdminJwtToken;

    protected User testUser;
    protected User adminUser;

    @BeforeEach
    void setUp() {
        createTestUsers();
    }

    private void createTestUsers() {
        // Create test user if not exists
        if (userRepository.findByUsername("testuser").isEmpty()) {
            testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail("test@example.com");
            testUser.setPassword(passwordEncoder.encode("Test@123"));
            testUser.setPhoneNumber("+1234567890");
            testUser.setAddress("123 Test St");
            testUser.setEmailVerified(true);
            testUser.setRoles(Set.of("USER"));
            testUser = userRepository.save(testUser);
        }

        // Create admin user if not exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@groceryapp.com");
            adminUser.setPassword(passwordEncoder.encode("Admin@123"));
            adminUser.setPhoneNumber("+9876543210");
            adminUser.setAddress("Admin Office");
            adminUser.setEmailVerified(true);
            adminUser.setRoles(Set.of("ADMIN"));
            adminUser = userRepository.save(adminUser);
        }
    }

    protected String getAuthHeader(boolean isAdmin) {
        return "Bearer " + (isAdmin ? testAdminJwtToken : testJwtToken);
    }

    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }

    protected void clearDatabase() {
        userRepository.deleteAll();
    }
}
