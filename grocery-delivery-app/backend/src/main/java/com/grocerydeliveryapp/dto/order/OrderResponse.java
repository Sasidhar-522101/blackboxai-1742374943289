package com.grocerydeliveryapp.dto.order;

import com.grocerydeliveryapp.model.OrderStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private List<OrderItemResponse> orderItems;
    private BigDecimal subtotal;
    private BigDecimal deliveryCharge;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String statusDescription;
    private String deliveryAddress;
    private String deliveryInstructions;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;
    
    // Delivery Information
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private String deliveryPartnerName;
    private String deliveryPartnerPhone;
    private String deliveryStatus;
    private Double deliveryProgress; // 0 to 100
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Customer Information
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    // Billing Information
    private String billingName;
    private String billingAddress;
    private String billingEmail;
    private String billingPhone;
    
    // Gift Information
    private boolean isGift;
    private String giftMessage;
    private boolean giftWrapped;
    
    // Rating and Feedback
    private Boolean isRated;
    private Integer rating;
    private String feedback;
    
    // Cancellation Information
    private String cancellationReason;
    private LocalDateTime cancellationTime;
    
    // Additional Information
    private String couponApplied;
    private BigDecimal discountAmount;
    private String specialInstructions;
    private boolean contactlessDelivery;
    
    // Tracking Information
    private List<OrderTrackingEvent> trackingEvents;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private String productImage;
        private Integer quantity;
        private BigDecimal priceAtTime;
        private BigDecimal discountAtTime;
        private BigDecimal subtotal;
        private String unit;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderTrackingEvent {
        private LocalDateTime timestamp;
        private String status;
        private String description;
        private String location;
    }
    
    // Helper method to calculate delivery progress based on status
    public void calculateDeliveryProgress() {
        switch (status) {
            case PENDING:
                this.deliveryProgress = 0.0;
                break;
            case CONFIRMED:
                this.deliveryProgress = 25.0;
                break;
            case PREPARING:
                this.deliveryProgress = 50.0;
                break;
            case OUT_FOR_DELIVERY:
                this.deliveryProgress = 75.0;
                break;
            case DELIVERED:
                this.deliveryProgress = 100.0;
                break;
            default:
                this.deliveryProgress = 0.0;
        }
    }
    
    // Helper method to generate status description
    public void generateStatusDescription() {
        switch (status) {
            case PENDING:
                this.statusDescription = "Order is pending confirmation";
                break;
            case CONFIRMED:
                this.statusDescription = "Order has been confirmed and is being processed";
                break;
            case PREPARING:
                this.statusDescription = "Your order is being prepared";
                break;
            case OUT_FOR_DELIVERY:
                this.statusDescription = "Your order is out for delivery";
                break;
            case DELIVERED:
                this.statusDescription = "Order has been delivered successfully";
                break;
            case CANCELLED:
                this.statusDescription = "Order has been cancelled";
                break;
            case REFUNDED:
                this.statusDescription = "Order has been refunded";
                break;
            default:
                this.statusDescription = "Status unknown";
        }
    }
}
