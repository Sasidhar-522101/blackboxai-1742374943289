package com.grocerydeliveryapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SwaggerConfigTest {

    @Autowired
    private SwaggerConfig swaggerConfig;

    @Test
    void openAPIConfigurationTest() {
        // Act
        OpenAPI openAPI = swaggerConfig.openAPI();

        // Assert
        assertNotNull(openAPI);
        
        // Verify Info object
        Info info = openAPI.getInfo();
        assertNotNull(info);
        assertEquals("Grocery Delivery App API", info.getTitle());
        assertEquals("1.0.0", info.getVersion());
        assertTrue(info.getDescription().contains("RESTful API documentation"));

        // Verify Contact information
        Contact contact = info.getContact();
        assertNotNull(contact);
        assertEquals("Grocery Delivery App Team", contact.getName());
        assertEquals("support@groceryapp.com", contact.getEmail());
        assertEquals("https://groceryapp.com", contact.getUrl());

        // Verify License information
        License license = info.getLicense();
        assertNotNull(license);
        assertEquals("Apache 2.0", license.getName());
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0", license.getUrl());
    }

    @Test
    void securitySchemeConfigurationTest() {
        // Act
        OpenAPI openAPI = swaggerConfig.openAPI();

        // Assert
        Components components = openAPI.getComponents();
        assertNotNull(components);

        // Verify Security Scheme
        SecurityScheme securityScheme = components.getSecuritySchemes().get("Bearer Authentication");
        assertNotNull(securityScheme);
        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType());
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());
    }

    @Test
    void securityRequirementConfigurationTest() {
        // Act
        OpenAPI openAPI = swaggerConfig.openAPI();

        // Assert
        assertFalse(openAPI.getSecurity().isEmpty());
        SecurityRequirement securityRequirement = openAPI.getSecurity().get(0);
        assertTrue(securityRequirement.containsKey("Bearer Authentication"));
    }

    @Test
    void componentsConfigurationTest() {
        // Act
        OpenAPI openAPI = swaggerConfig.openAPI();

        // Assert
        Components components = openAPI.getComponents();
        assertNotNull(components);
        assertNotNull(components.getSecuritySchemes());
        assertTrue(components.getSecuritySchemes().containsKey("Bearer Authentication"));
    }

    @Test
    void verifyRequiredFieldsPresent() {
        // Act
        OpenAPI openAPI = swaggerConfig.openAPI();
        Info info = openAPI.getInfo();

        // Assert required fields
        assertNotNull(info.getTitle());
        assertNotNull(info.getVersion());
        assertNotNull(info.getDescription());
        assertNotNull(info.getContact().getName());
        assertNotNull(info.getContact().getEmail());
        assertNotNull(info.getLicense().getName());
        assertNotNull(info.getLicense().getUrl());
    }

    @Test
    void verifySecuritySchemeDetails() {
        // Act
        OpenAPI openAPI = swaggerConfig.openAPI();
        SecurityScheme securityScheme = openAPI.getComponents()
            .getSecuritySchemes()
            .get("Bearer Authentication");

        // Assert security scheme details
        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType());
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());
    }

    @Test
    void verifyContactInformation() {
        // Act
        OpenAPI openAPI = swaggerConfig.openAPI();
        Contact contact = openAPI.getInfo().getContact();

        // Assert contact information
        assertEquals("Grocery Delivery App Team", contact.getName());
        assertEquals("support@groceryapp.com", contact.getEmail());
        assertEquals("https://groceryapp.com", contact.getUrl());
    }

    @Test
    void verifyLicenseInformation() {
        // Act
        OpenAPI openAPI = swaggerConfig.openAPI();
        License license = openAPI.getInfo().getLicense();

        // Assert license information
        assertEquals("Apache 2.0", license.getName());
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0", license.getUrl());
    }

    @Test
    void verifySecurityRequirements() {
        // Act
        OpenAPI openAPI = swaggerConfig.openAPI();

        // Assert security requirements
        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
        
        SecurityRequirement requirement = openAPI.getSecurity().get(0);
        assertTrue(requirement.containsKey("Bearer Authentication"));
        assertFalse(requirement.get("Bearer Authentication").isEmpty());
    }

    @Test
    void verifyAPIInformation() {
        // Act
        OpenAPI openAPI = swaggerConfig.openAPI();
        Info info = openAPI.getInfo();

        // Assert API information
        assertEquals("Grocery Delivery App API", info.getTitle());
        assertEquals("1.0.0", info.getVersion());
        assertTrue(info.getDescription().contains("RESTful API documentation"));
    }
}
