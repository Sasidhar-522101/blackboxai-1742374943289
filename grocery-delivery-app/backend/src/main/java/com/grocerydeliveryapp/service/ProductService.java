package com.grocerydeliveryapp.service;

import com.grocerydeliveryapp.dto.product.ProductRequest;
import com.grocerydeliveryapp.dto.product.ProductResponse;
import com.grocerydeliveryapp.model.Product;
import com.grocerydeliveryapp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::mapToProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable)
                .map(this::mapToProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable)
                .map(this::mapToProductResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        updateProductFromRequest(product, request);
        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        updateProductFromRequest(product, request);
        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrue()
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable)
                .map(this::mapToProductResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsOnDiscount() {
        return productRepository.findByDiscountPercentageGreaterThan(0.0)
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsWithFilters(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String brand,
            Pageable pageable) {
        return productRepository.findProductsWithFilters(category, minPrice, maxPrice, brand, pageable)
                .map(this::mapToProductResponse);
    }

    @Transactional
    public void updateStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
        
        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }
        
        product.setStockQuantity(product.getStockQuantity() - quantity);
        if (product.getStockQuantity() == 0) {
            product.setAvailable(false);
        }
        productRepository.save(product);
    }

    private void updateProductFromRequest(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        product.setDiscountPercentage(request.getDiscountPercentage());
        product.setUnit(request.getUnit());
        product.setBrand(request.getBrand());
        product.setFeatured(request.isFeatured());
        product.setNutritionalInfo(request.getNutritionalInfo());
        product.setAvailable(request.getStockQuantity() > 0);
    }

    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse response = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .isAvailable(product.isAvailable())
                .discountPercentage(product.getDiscountPercentage())
                .unit(product.getUnit())
                .brand(product.getBrand())
                .isFeatured(product.isFeatured())
                .nutritionalInfo(product.getNutritionalInfo())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();

        response.calculateDerivedFields();
        return response;
    }
}
