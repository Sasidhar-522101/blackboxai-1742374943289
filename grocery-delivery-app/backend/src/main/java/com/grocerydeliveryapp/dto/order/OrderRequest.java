package com.grocerydeliveryapp.dto.order;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotEmpty(message = "Order must contain at least one item")
    private List<@Valid OrderItemRequest> orderItems;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    private String deliveryInstructions;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    // For UPI payments
    private String upiId;

    // For card payments
    private String cardNumber;
    private String cardExpiryMonth;
    private String cardExpiryYear;
    private String cardCvv;

    // For saved addresses
    private Long savedAddressId;

    // For delivery time slot
    private String preferredDeliveryTime;

    // For applying coupon
    private String couponCode;

    // For special instructions
    private String specialInstructions;

    // For contact preferences
    private boolean contactlessDelivery;
    private String alternatePhoneNumber;

    // For billing
    private String billingName;
    private String billingEmail;
    private String billingPhone;
    private boolean differentBillingAddress;
    private String billingAddress;

    // For gift options
    private boolean isGift;
    private String giftMessage;
    private boolean giftWrap;
}
