package com.grocerydeliveryapp.config;

import com.grocerydeliveryapp.model.Product;
import com.grocerydeliveryapp.model.User;
import com.grocerydeliveryapp.repository.ProductRepository;
import com.grocerydeliveryapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsers();
        }
        
        if (productRepository.count() == 0) {
            seedProducts();
        }
    }

    private void seedUsers() {
        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@groceryapp.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRoles(Set.of("ADMIN"));
        admin.setEmailVerified(true);
        admin.setPhoneNumber("+1234567890");
        admin.setAddress("Admin Office");
        userRepository.save(admin);

        // Create test user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("Test@123"));
        user.setRoles(Set.of("USER"));
        user.setEmailVerified(true);
        user.setPhoneNumber("+9876543210");
        user.setAddress("123 Test Street");
        userRepository.save(user);
    }

    private void seedProducts() {
        List<Product> products = Arrays.asList(
            // Fruits & Vegetables
            createProduct("Fresh Apples", "Sweet and crispy red apples", new BigDecimal("120"), 100,
                    "Fruits", "https://example.com/images/apples.jpg", 0.0, "kg", "Local Farm", true),
            createProduct("Organic Bananas", "Fresh organic bananas", new BigDecimal("60"), 150,
                    "Fruits", "https://example.com/images/bananas.jpg", 0.0, "dozen", "Organic Farms", false),
            createProduct("Fresh Tomatoes", "Ripe red tomatoes", new BigDecimal("40"), 200,
                    "Vegetables", "https://example.com/images/tomatoes.jpg", 10.0, "kg", "Local Farm", false),
            
            // Dairy Products
            createProduct("Fresh Milk", "Farm fresh milk", new BigDecimal("60"), 50,
                    "Dairy", "https://example.com/images/milk.jpg", 0.0, "litre", "Farm Fresh", true),
            createProduct("Greek Yogurt", "Creamy Greek yogurt", new BigDecimal("80"), 40,
                    "Dairy", "https://example.com/images/yogurt.jpg", 5.0, "pack", "Dairy Best", false),
            
            // Bakery
            createProduct("Whole Wheat Bread", "Fresh baked whole wheat bread", new BigDecimal("45"), 30,
                    "Bakery", "https://example.com/images/bread.jpg", 0.0, "loaf", "Fresh Bake", true),
            createProduct("Croissants", "Butter croissants", new BigDecimal("60"), 25,
                    "Bakery", "https://example.com/images/croissants.jpg", 15.0, "pack", "French Bakery", false),
            
            // Snacks
            createProduct("Potato Chips", "Crispy potato chips", new BigDecimal("30"), 100,
                    "Snacks", "https://example.com/images/chips.jpg", 0.0, "pack", "Snack King", false),
            createProduct("Mixed Nuts", "Premium mixed nuts", new BigDecimal("250"), 40,
                    "Snacks", "https://example.com/images/nuts.jpg", 5.0, "pack", "Nutty's", true),
            
            // Beverages
            createProduct("Orange Juice", "Fresh orange juice", new BigDecimal("90"), 60,
                    "Beverages", "https://example.com/images/juice.jpg", 0.0, "litre", "Fresh Squeeze", false),
            createProduct("Green Tea", "Organic green tea bags", new BigDecimal("150"), 45,
                    "Beverages", "https://example.com/images/tea.jpg", 10.0, "pack", "Tea Master", true),
            
            // Household
            createProduct("Dish Soap", "Liquid dish washing soap", new BigDecimal("85"), 70,
                    "Household", "https://example.com/images/dish-soap.jpg", 0.0, "bottle", "Clean Pro", false),
            createProduct("Paper Towels", "Absorbent paper towels", new BigDecimal("120"), 80,
                    "Household", "https://example.com/images/paper-towels.jpg", 5.0, "pack", "Clean Pro", false)
        );

        productRepository.saveAll(products);
    }

    private Product createProduct(String name, String description, BigDecimal price, Integer stockQuantity,
                                String category, String imageUrl, Double discountPercentage, String unit,
                                String brand, boolean isFeatured) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        product.setDiscountPercentage(discountPercentage);
        product.setUnit(unit);
        product.setBrand(brand);
        product.setFeatured(isFeatured);
        product.setAvailable(true);
        product.setNutritionalInfo("Sample nutritional information");
        return product;
    }
}
