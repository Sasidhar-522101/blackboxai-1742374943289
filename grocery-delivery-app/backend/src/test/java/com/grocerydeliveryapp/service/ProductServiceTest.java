package com.grocerydeliveryapp.service;

import com.grocerydeliveryapp.dto.product.ProductRequest;
import com.grocerydeliveryapp.dto.product.ProductResponse;
import com.grocerydeliveryapp.exception.OutOfStockException;
import com.grocerydeliveryapp.model.Product;
import com.grocerydeliveryapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductRequest productRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup test product
        testProduct = new Product();
        testProduct.setId(1L);
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

        // Setup product request
        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setDescription("Test Description");
        productRequest.setPrice(new BigDecimal("99.99"));
        productRequest.setStockQuantity(100);
        productRequest.setCategory("Test Category");
        productRequest.setImageUrl("test-image.jpg");
        productRequest.setDiscountPercentage(10.0);
        productRequest.setUnit("piece");
        productRequest.setBrand("Test Brand");

        // Setup pageable
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllProductsSuccess() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        // Act
        Page<ProductResponse> result = productService.getAllProducts(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testProduct.getName(), result.getContent().get(0).getName());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void getProductByIdSuccess() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        ProductResponse result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getPrice(), result.getPrice());
    }

    @Test
    void getProductByIdNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            productService.getProductById(1L);
        });
    }

    @Test
    void createProductSuccess() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductResponse result = productService.createProduct(productRequest);

        // Assert
        assertNotNull(result);
        assertEquals(productRequest.getName(), result.getName());
        assertEquals(productRequest.getPrice(), result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProductSuccess() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productRequest.setName("Updated Product");
        productRequest.setPrice(new BigDecimal("149.99"));

        // Act
        ProductResponse result = productService.updateProduct(1L, productRequest);

        // Assert
        assertNotNull(result);
        assertEquals(productRequest.getName(), result.getName());
        assertEquals(productRequest.getPrice(), result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateStockSuccess() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        productService.updateStock(1L, 10);

        // Assert
        assertEquals(90, testProduct.getStockQuantity());
        assertTrue(testProduct.isAvailable());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void updateStockInsufficientQuantity() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            productService.updateStock(1L, 150);
        });
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductsByPriceRangeSuccess() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());
        when(productRepository.findByPriceBetween(any(BigDecimal.class), any(BigDecimal.class), any(Pageable.class)))
                .thenReturn(productPage);

        // Act
        Page<ProductResponse> result = productService.getProductsByPriceRange(
                new BigDecimal("50.00"), 
                new BigDecimal("150.00"), 
                pageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testProduct.getName(), result.getContent().get(0).getName());
    }

    @Test
    void getProductsOnDiscountSuccess() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByDiscountPercentageGreaterThan(0.0))
                .thenReturn(products);

        // Act
        List<ProductResponse> result = productService.getProductsOnDiscount();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(testProduct.getName(), result.get(0).getName());
        assertEquals(testProduct.getDiscountPercentage(), result.get(0).getDiscountPercentage());
    }
}
