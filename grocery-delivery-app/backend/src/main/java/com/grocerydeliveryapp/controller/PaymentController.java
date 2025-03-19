package com.grocerydeliveryapp.controller;

import com.grocerydeliveryapp.dto.order.OrderResponse;
import com.grocerydeliveryapp.exception.PaymentException;
import com.grocerydeliveryapp.model.Order;
import com.grocerydeliveryapp.service.OrderService;
import com.grocerydeliveryapp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}", maxAge = 3600)
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping("/process/{orderId}")
    public ResponseEntity<Map<String, Object>> processPayment(
            @PathVariable Long orderId,
            @RequestParam String paymentMethod,
            @RequestBody(required = false) Map<String, String> paymentDetails,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        OrderResponse orderResponse = orderService.getOrderById(orderId, userId);
        Order order = orderService.getOrderEntity(orderId); // You'll need to add this method to OrderService

        try {
            Map<String, Object> paymentResult = paymentService.processPayment(order, paymentMethod, paymentDetails);
            
            // Update order status after successful payment
            if (!"COD".equalsIgnoreCase(paymentMethod)) {
                orderService.updateOrderPaymentStatus(orderId, "PAID", paymentResult.get("transactionId").toString());
            }

            return ResponseEntity.ok(paymentResult);
        } catch (PaymentException e) {
            // Payment failed, update order status
            orderService.updateOrderPaymentStatus(orderId, "FAILED", e.getTransactionId());
            throw e;
        }
    }

    @PostMapping("/verify-upi")
    public ResponseEntity<Boolean> verifyUpiId(@RequestParam String upiId) {
        // Simple UPI ID validation
        boolean isValid = upiId != null && upiId.contains("@") && !upiId.contains(" ");
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/validate-card")
    public ResponseEntity<Map<String, Object>> validateCard(@RequestBody Map<String, String> cardDetails) {
        try {
            // This would typically integrate with a payment gateway
            // For demo purposes, we'll just do basic validation
            String cardNumber = cardDetails.get("cardNumber");
            String expiryMonth = cardDetails.get("expiryMonth");
            String expiryYear = cardDetails.get("expiryYear");
            String cvv = cardDetails.get("cvv");

            boolean isValid = cardNumber != null && cardNumber.matches("\\d{16}") &&
                            expiryMonth != null && expiryMonth.matches("(0[1-9]|1[0-2])") &&
                            expiryYear != null && expiryYear.matches("\\d{4}") &&
                            cvv != null && cvv.matches("\\d{3}");

            return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "cardType", detectCardType(cardNumber),
                "message", isValid ? "Card details are valid" : "Invalid card details"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "message", "Error validating card details"
            ));
        }
    }

    @GetMapping("/order/{orderId}/bill")
    public ResponseEntity<Map<String, Object>> getDigitalBill(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.parseLong(userDetails.getUsername());
        OrderResponse orderResponse = orderService.getOrderById(orderId, userId);
        Order order = orderService.getOrderEntity(orderId);

        Map<String, Object> bill = paymentService.generateDigitalBill(
            order, 
            orderResponse.getTransactionId(), 
            orderResponse.getPaymentMethod()
        );

        return ResponseEntity.ok(bill);
    }

    private String detectCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 2) {
            return "Unknown";
        }

        String prefix = cardNumber.substring(0, 2);
        switch (prefix.charAt(0)) {
            case '4':
                return "Visa";
            case '5':
                return "MasterCard";
            case '3':
                if (prefix.equals("34") || prefix.equals("37")) {
                    return "American Express";
                }
                return "Unknown";
            case '6':
                return "Discover";
            default:
                return "Unknown";
        }
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentException(PaymentException e) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", true,
            "message", e.getMessage(),
            "errorType", e.getErrorType().toString(),
            "transactionId", e.getTransactionId() != null ? e.getTransactionId() : ""
        ));
    }
}
