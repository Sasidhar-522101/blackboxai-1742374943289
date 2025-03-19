package com.grocerydeliveryapp.integration;

import com.grocerydeliveryapp.dto.product.ProductRequest;
import com.grocerydeliveryapp.dto.product.ProductResponse;
import com.grocerydeliveryapp.model.Product;
import com.grocerydeliveryapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class ProductControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setupTestProduct() {
        // Create a test product
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(100);
        testProduct.setCategory("Test Category");
        testProduct.setImageUrl("test-image.jpg");
        testProduct.setDiscountPercentage(10.0);
        testProduct.setUnit("piece");
        testProduct.setBrand("Test Brand");
        testProduct.setAvailable(true);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void getAllProductsSuccess() throws Exception {
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andReturn();

        // Verify response contains products
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("Test Product"));
    }

    @Test
    void getProductByIdSuccess() throws Exception {
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/products/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andReturn();

        ProductResponse response = fromJson(result.getResponse().getContentAsString(), ProductResponse.class);
        assertEquals(testProduct.getName(), response.getName());
        assertEquals(testProduct.getPrice(), response.getPrice());
    }

    @Test
    void getProductByIdNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProductSuccess() throws Exception {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("New Product");
        request.setDescription("New Description");
        request.setPrice(new BigDecimal("149.99"));
        request.setStockQuantity(50);
        request.setCategory("New Category");
        request.setImageUrl("new-image.jpg");
        request.setDiscountPercentage(5.0);
        request.setUnit("piece");
        request.setBrand("New Brand");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/products")
                .header("Authorization", getAuthHeader(true))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        ProductResponse response = fromJson(result.getResponse().getContentAsString(), ProductResponse.class);
        assertEquals(request.getName(), response.getName());
        assertEquals(request.getPrice(), response.getPrice());
    }

    @Test
    void createProductUnauthorized() throws Exception {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("New Product");

        // Act & Assert - Without auth header
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isUnauthorized());

        // Act & Assert - With non-admin auth
        mockMvc.perform(post("/api/products")
                .header("Authorization", getAuthHeader(false))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProductSuccess() throws Exception {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Updated Product");
        request.setDescription("Updated Description");
        request.setPrice(new BigDecimal("199.99"));
        request.setStockQuantity(75);
        request.setCategory(testProduct.getCategory());
        request.setImageUrl(testProduct.getImageUrl());
        request.setDiscountPercentage(15.0);
        request.setUnit(testProduct.getUnit());
        request.setBrand(testProduct.getBrand());

        // Act & Assert
        MvcResult result = mockMvc.perform(put("/api/products/{id}", testProduct.getId())
                .header("Authorization", getAuthHeader(true))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        ProductResponse response = fromJson(result.getResponse().getContentAsString(), ProductResponse.class);
        assertEquals(request.getName(), response.getName());
        assertEquals(request.getPrice(), response.getPrice());
    }

    @Test
    void deleteProductSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/products/{id}", testProduct.getId())
                .header("Authorization", getAuthHeader(true)))
                .andExpect(status().isNoContent());

        // Verify product is deleted
        assertFalse(productRepository.existsById(testProduct.getId()));
    }

    @Test
    void searchProductsSuccess() throws Exception {
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/products/search")
                .param("keyword", "Test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        // Verify search results
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("Test Product"));
    }

    @Test
    void getProductsByPriceRangeSuccess() throws Exception {
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/products/price-range")
                .param("minPrice", "50")
                .param("maxPrice", "150")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        // Verify results
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("Test Product"));
    }

    @Test
    void getFeaturedProductsSuccess() throws Exception {
        // Update test product to be featured
        testProduct.setFeatured(true);
        productRepository.save(testProduct);

        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/products/featured"))
                .andExpect(status().isOk())
                .andReturn();

        // Verify featured products
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("Test Product"));
    }

    @Test
    void getProductsOnDiscountSuccess() throws Exception {
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/products/on-discount"))
                .andExpect(status().isOk())
                .andReturn();

        // Verify discounted products
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("Test Product"));
    }
}
