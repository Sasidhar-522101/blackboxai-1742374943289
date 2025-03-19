package com.grocerydeliveryapp.dto.product;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Product description is required")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity must not be negative")
    private Integer stockQuantity;

    @NotBlank(message = "Category is required")
    private String category;

    private String imageUrl;

    @PositiveOrZero(message = "Discount percentage must not be negative")
    private Double discountPercentage;

    @NotBlank(message = "Unit is required")
    private String unit;

    private String brand;

    private boolean isFeatured;

    private String nutritionalInfo;
}
