package com.grocerydeliveryapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();

    private BigDecimal totalAmount;

    private BigDecimal deliveryCharge;

    private BigDecimal taxAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String deliveryAddress;

    private String deliveryInstructions;

    private String paymentMethod;

    private String paymentStatus;

    private String transactionId;

    private LocalDateTime estimatedDeliveryTime;

    private LocalDateTime actualDeliveryTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String deliveryPartnerName;

    private String deliveryPartnerPhone;

    private String cancellationReason;

    private Boolean isRated;

    private Integer rating;

    private String feedback;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        calculateTotalAmount();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalAmount();
    }

    private void calculateTotalAmount() {
        if (orderItems != null && !orderItems.isEmpty()) {
            BigDecimal itemsTotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Add delivery charge if present
            if (deliveryCharge != null) {
                itemsTotal = itemsTotal.add(deliveryCharge);
            }

            // Add tax if present
            if (taxAmount != null) {
                itemsTotal = itemsTotal.add(taxAmount);
            }

            this.totalAmount = itemsTotal;
        }
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        calculateTotalAmount();
    }

    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        calculateTotalAmount();
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    public boolean isDelivered() {
        return status == OrderStatus.DELIVERED;
    }

    public boolean isInProgress() {
        return status == OrderStatus.PREPARING || status == OrderStatus.OUT_FOR_DELIVERY;
    }
}
