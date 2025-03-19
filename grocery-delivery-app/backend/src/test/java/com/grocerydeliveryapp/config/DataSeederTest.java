package com.grocerydeliveryapp.config;

import com.grocerydeliveryapp.model.Product;
import com.grocerydeliveryapp.model.User;
import com.grocerydeliveryapp.repository.ProductRepository;
import com.grocerydeliveryapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<List<Product>> productsCaptor;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    }

    @Test
    void runWithEmptyDatabase() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(productRepository.count()).thenReturn(0L);

        // Act
        dataSeeder.run();

        // Assert
        verify(userRepository, times(2)).save(userCaptor.capture());
        verify(productRepository).saveAll(productsCaptor.capture());

        // Verify users
        List<User> savedUsers = userCaptor.getAllValues();
        assertEquals(2, savedUsers.size());

        // Verify admin user
        User adminUser = savedUsers.get(0);
        assertEquals("admin", adminUser.getUsername());
        assertEquals("admin@groceryapp.com", adminUser.getEmail());
        assertTrue(adminUser.getRoles().contains("ADMIN"));
        assertTrue(adminUser.isEmailVerified());

        // Verify test user
        User testUser = savedUsers.get(1);
        assertEquals("testuser", testUser.getUsername());
        assertEquals("test@example.com", testUser.getEmail());
        assertTrue(testUser.getRoles().contains("USER"));
        assertTrue(testUser.isEmailVerified());

        // Verify products
        List<Product> savedProducts = productsCaptor.getValue();
        assertFalse(savedProducts.isEmpty());
        
        // Verify product categories
        assertTrue(savedProducts.stream().anyMatch(p -> "Fruits".equals(p.getCategory())));
        assertTrue(savedProducts.stream().anyMatch(p -> "Vegetables".equals(p.getCategory())));
        assertTrue(savedProducts.stream().anyMatch(p -> "Dairy".equals(p.getCategory())));
        assertTrue(savedProducts.stream().anyMatch(p -> "Bakery".equals(p.getCategory())));
    }

    @Test
    void runWithExistingData() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(2L);
        when(productRepository.count()).thenReturn(10L);

        // Act
        dataSeeder.run();

        // Assert
        verify(userRepository, never()).save(any(User.class));
        verify(productRepository, never()).saveAll(anyList());
    }

    @Test
    void verifyProductData() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(productRepository.count()).thenReturn(0L);

        // Act
        dataSeeder.run();

        // Assert
        verify(productRepository).saveAll(productsCaptor.capture());
        List<Product> products = productsCaptor.getValue();

        // Verify product details
        products.forEach(product -> {
            assertNotNull(product.getName());
            assertNotNull(product.getDescription());
            assertNotNull(product.getPrice());
            assertTrue(product.getPrice().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(product.getStockQuantity() > 0);
            assertNotNull(product.getCategory());
            assertNotNull(product.getImageUrl());
            assertNotNull(product.getUnit());
            assertNotNull(product.getBrand());
            assertTrue(product.isAvailable());
        });

        // Verify featured products
        long featuredCount = products.stream()
                .filter(Product::isFeatured)
                .count();
        assertTrue(featuredCount > 0);

        // Verify products with discounts
        long discountedCount = products.stream()
                .filter(p -> p.getDiscountPercentage() > 0)
                .count();
        assertTrue(discountedCount > 0);
    }

    @Test
    void verifyUserRoles() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(productRepository.count()).thenReturn(0L);

        // Act
        dataSeeder.run();

        // Assert
        verify(userRepository, times(2)).save(userCaptor.capture());
        List<User> users = userCaptor.getAllValues();

        // Verify admin roles
        User admin = users.stream()
                .filter(u -> "admin".equals(u.getUsername()))
                .findFirst()
                .orElseThrow();
        assertTrue(admin.getRoles().contains("ADMIN"));

        // Verify user roles
        User user = users.stream()
                .filter(u -> "testuser".equals(u.getUsername()))
                .findFirst()
                .orElseThrow();
        assertTrue(user.getRoles().contains("USER"));
        assertFalse(user.getRoles().contains("ADMIN"));
    }

    @Test
    void verifyPasswordEncoding() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(productRepository.count()).thenReturn(0L);

        // Act
        dataSeeder.run();

        // Assert
        verify(passwordEncoder, times(2)).encode(anyString());
        verify(userRepository, times(2)).save(userCaptor.capture());

        List<User> users = userCaptor.getAllValues();
        users.forEach(user -> {
            assertEquals("encodedPassword", user.getPassword());
        });
    }

    @Test
    void verifyProductCategories() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(productRepository.count()).thenReturn(0L);

        // Act
        dataSeeder.run();

        // Assert
        verify(productRepository).saveAll(productsCaptor.capture());
        List<Product> products = productsCaptor.getValue();

        // Get unique categories
        List<String> categories = products.stream()
                .map(Product::getCategory)
                .distinct()
                .toList();

        // Verify essential categories exist
        assertTrue(categories.contains("Fruits"));
        assertTrue(categories.contains("Vegetables"));
        assertTrue(categories.contains("Dairy"));
        assertTrue(categories.contains("Bakery"));
        assertTrue(categories.contains("Snacks"));
        assertTrue(categories.contains("Beverages"));
        assertTrue(categories.contains("Household"));
    }
}
