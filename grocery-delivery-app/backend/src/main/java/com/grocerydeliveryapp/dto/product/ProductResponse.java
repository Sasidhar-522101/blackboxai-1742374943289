package com.grocerydeliveryapp.dto.product;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private Integer stockQuantity;
    private String category;
    private String imageUrl;
    private boolean isAvailable;
    private Double discountPercentage;
    private String unit;
    private String brand;
    private boolean isFeatured;
    private String nutritionalInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for frontend display
    private boolean isInStock;
    private String formattedPrice;
    private String formattedDiscountedPrice;
    private String discountTag;
    private String stockStatus;
    
    public void calculateDerivedFields() {
        // Calculate discounted price
        if (discountPercentage != null && discountPercentage > 0) {
            BigDecimal discount = price.multiply(BigDecimal.valueOf(discountPercentage / 100));
            this.discountedPrice = price.subtract(discount);
        } else {
            this.discountedPrice = price;
        }

        // Format prices
        this.formattedPrice = String.format("₹%.2f", price);
        this.formattedDiscountedPrice = String.format("₹%.2f", discountedPrice);

        // Set discount tag
        if (discountPercentage != null && discountPercentage > 0) {
            this.discountTag = String.format("%.0f%% OFF", discountPercentage);
        }

        // Set stock status
        this.isInStock = stockQuantity > 0 && isAvailable;
        if (!isAvailable) {
            this.stockStatus = "Currently Unavailable";
        } else if (stockQuantity == 0) {
            this.stockStatus = "Out of Stock";
        } else if (stockQuantity < 5) {
            this.stockStatus = "Low Stock";
        } else {
            this.stockStatus = "In Stock";
        }
    }
}
