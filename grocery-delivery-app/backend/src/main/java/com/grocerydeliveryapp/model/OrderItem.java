package com.grocerydeliveryapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Positive
    private Integer quantity;

    @NotNull
    private BigDecimal priceAtTime; // Price at the time of order

    private BigDecimal discountAtTime; // Discount at the time of order

    @Column(name = "subtotal")
    private BigDecimal subtotal; // Price * Quantity - Discount

    @PrePersist
    @PreUpdate
    protected void calculateSubtotal() {
        if (priceAtTime != null && quantity != null) {
            BigDecimal total = priceAtTime.multiply(BigDecimal.valueOf(quantity));
            if (discountAtTime != null) {
                total = total.subtract(discountAtTime);
            }
            this.subtotal = total;
        }
    }
}
