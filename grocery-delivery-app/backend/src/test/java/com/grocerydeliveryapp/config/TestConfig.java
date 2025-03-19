package com.grocerydeliveryapp.config;

import com.grocerydeliveryapp.security.JwtUtil;
import com.grocerydeliveryapp.service.EmailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.thymeleaf.spring5.SpringTemplateEngine;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }

    @Bean
    @Primary
    public EmailService emailService() {
        return mock(EmailService.class);
    }

    @Bean
    @Primary
    public SpringTemplateEngine templateEngine() {
        return mock(SpringTemplateEngine.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        return new JwtUtil("testSecretKey2023ForTestingPurposesOnly", 3600000);
    }

    @Bean
    public MockMvc mockMvc(WebApplicationContext webApplicationContext) {
        return MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Bean
    public String testJwtToken() {
        return jwtUtil().generateToken(org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build());
    }

    @Bean
    public String testAdminJwtToken() {
        return jwtUtil().generateToken(org.springframework.security.core.userdetails.User
                .withUsername("admin")
                .password("password")
                .authorities("ROLE_ADMIN")
                .build());
    }
}
