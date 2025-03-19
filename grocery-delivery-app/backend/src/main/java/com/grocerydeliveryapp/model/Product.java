package com.grocerydeliveryapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @Positive
    private Integer stockQuantity;

    @NotBlank
    private String category;

    private String imageUrl;

    private boolean isAvailable = true;

    private Double discountPercentage;

    @NotNull
    private String unit; // e.g., kg, pieces, packets

    private String brand;

    private boolean isFeatured;

    @Column(length = 1000)
    private String nutritionalInfo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Method to check if product is in stock
    public boolean isInStock() {
        return stockQuantity > 0 && isAvailable;
    }

    // Method to calculate discounted price
    public BigDecimal getDiscountedPrice() {
        if (discountPercentage == null || discountPercentage == 0) {
            return price;
        }
        BigDecimal discount = price.multiply(BigDecimal.valueOf(discountPercentage / 100));
        return price.subtract(discount);
    }
}
