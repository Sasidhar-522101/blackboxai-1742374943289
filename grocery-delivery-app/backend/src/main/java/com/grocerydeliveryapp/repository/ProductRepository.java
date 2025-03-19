package com.grocerydeliveryapp.repository;

import com.grocerydeliveryapp.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find products by category
    Page<Product> findByCategory(String category, Pageable pageable);
    
    // Search products by name or description containing keyword
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    // Find products by price range
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    // Find featured products
    List<Product> findByIsFeaturedTrue();
    
    // Find products by category and availability
    Page<Product> findByCategoryAndIsAvailableTrue(String category, Pageable pageable);
    
    // Find products with discount
    List<Product> findByDiscountPercentageGreaterThan(Double discount);
    
    // Find products by brand
    List<Product> findByBrand(String brand);
    
    // Find products with low stock (less than specified quantity)
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold")
    List<Product> findProductsWithLowStock(@Param("threshold") Integer threshold);
    
    // Find products by multiple categories
    List<Product> findByCategoryIn(List<String> categories);
    
    // Count products by category
    Long countByCategory(String category);
    
    // Find available products sorted by price
    @Query("SELECT p FROM Product p WHERE p.isAvailable = true ORDER BY p.price")
    Page<Product> findAvailableProductsSortedByPrice(Pageable pageable);
    
    // Search products with filters
    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:brand IS NULL OR p.brand = :brand) AND " +
           "p.isAvailable = true")
    Page<Product> findProductsWithFilters(
        @Param("category") String category,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("brand") String brand,
        Pageable pageable
    );
}
