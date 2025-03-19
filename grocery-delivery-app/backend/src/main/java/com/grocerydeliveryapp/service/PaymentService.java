package com.grocerydeliveryapp.service;

import com.grocerydeliveryapp.exception.PaymentException;
import com.grocerydeliveryapp.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final EmailService emailService;

    @Transactional
    public Map<String, Object> processPayment(Order order, String paymentMethod, Map<String, String> paymentDetails) {
        String transactionId = generateTransactionId();
        
        try {
            switch (paymentMethod.toUpperCase()) {
                case "CARD":
                    return processCardPayment(order, paymentDetails, transactionId);
                case "UPI":
                    return processUpiPayment(order, paymentDetails, transactionId);
                case "COD":
                    return processCashOnDelivery(order, transactionId);
                default:
                    throw new PaymentException("Unsupported payment method: " + paymentMethod);
            }
        } catch (PaymentException e) {
            // Re-throw PaymentException as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions in PaymentException
            throw new PaymentException("Payment processing failed", transactionId, 
                    PaymentException.PaymentErrorType.GENERAL_ERROR);
        }
    }

    private Map<String, Object> processCardPayment(Order order, Map<String, String> paymentDetails, String transactionId) {
        // Validate card details
        validateCardDetails(paymentDetails);

        // Simulate payment processing
        simulatePaymentProcessing();

        // Generate digital bill
        Map<String, Object> bill = generateDigitalBill(order, transactionId, "CARD");

        // Send payment confirmation email
        sendPaymentConfirmationEmail(order, bill);

        return bill;
    }

    private Map<String, Object> processUpiPayment(Order order, Map<String, String> paymentDetails, String transactionId) {
        // Validate UPI ID
        String upiId = paymentDetails.get("upiId");
        if (upiId == null || !upiId.contains("@")) {
            throw PaymentException.invalidUpi(upiId);
        }

        // Simulate UPI payment processing
        simulatePaymentProcessing();

        // Generate digital bill
        Map<String, Object> bill = generateDigitalBill(order, transactionId, "UPI");

        // Send payment confirmation email
        sendPaymentConfirmationEmail(order, bill);

        return bill;
    }

    private Map<String, Object> processCashOnDelivery(Order order, String transactionId) {
        // Generate digital bill for COD
        Map<String, Object> bill = generateDigitalBill(order, transactionId, "COD");

        // Send order confirmation email with COD details
        sendCodConfirmationEmail(order, bill);

        return bill;
    }

    private void validateCardDetails(Map<String, String> paymentDetails) {
        String cardNumber = paymentDetails.get("cardNumber");
        String expiryMonth = paymentDetails.get("expiryMonth");
        String expiryYear = paymentDetails.get("expiryYear");
        String cvv = paymentDetails.get("cvv");

        // Basic card number validation (Luhn algorithm can be implemented for production)
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            throw PaymentException.invalidCard("Invalid card number");
        }

        // Expiry date validation
        if (expiryMonth == null || expiryYear == null || 
            !expiryMonth.matches("(0[1-9]|1[0-2])") || !expiryYear.matches("\\d{4}")) {
            throw PaymentException.invalidCard("Invalid expiry date");
        }

        // CVV validation
        if (cvv == null || !cvv.matches("\\d{3}")) {
            throw PaymentException.invalidCard("Invalid CVV");
        }

        // Check if card is expired
        int month = Integer.parseInt(expiryMonth);
        int year = Integer.parseInt(expiryYear);
        LocalDateTime now = LocalDateTime.now();
        if (year < now.getYear() || (year == now.getYear() && month < now.getMonthValue())) {
            throw PaymentException.invalidCard("Card has expired");
        }
    }

    private void simulatePaymentProcessing() {
        try {
            // Simulate payment processing delay
            Thread.sleep(2000);
            
            // Simulate random payment failures (for testing purposes)
            if (Math.random() < 0.1) { // 10% chance of failure
                throw new PaymentException("Payment processing failed", 
                    PaymentException.PaymentErrorType.PAYMENT_GATEWAY_ERROR);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentException("Payment processing was interrupted");
        }
    }

    private Map<String, Object> generateDigitalBill(Order order, String transactionId, String paymentMethod) {
        Map<String, Object> bill = new HashMap<>();
        
        // Basic bill information
        bill.put("billNumber", "BILL-" + transactionId);
        bill.put("orderNumber", order.getTransactionId());
        bill.put("dateTime", LocalDateTime.now().toString());
        bill.put("paymentMethod", paymentMethod);
        bill.put("transactionId", transactionId);

        // Customer information
        bill.put("customerName", order.getUser().getUsername());
        bill.put("customerEmail", order.getUser().getEmail());
        bill.put("deliveryAddress", order.getDeliveryAddress());

        // Order items
        bill.put("items", order.getOrderItems().stream()
                .map(item -> Map.of(
                    "name", item.getProduct().getName(),
                    "quantity", item.getQuantity(),
                    "price", item.getPriceAtTime(),
                    "discount", item.getDiscountAtTime(),
                    "subtotal", item.getSubtotal()
                ))
                .toList());

        // Totals
        bill.put("subtotal", order.getTotalAmount().subtract(order.getDeliveryCharge()).subtract(order.getTaxAmount()));
        bill.put("deliveryCharge", order.getDeliveryCharge());
        bill.put("tax", order.getTaxAmount());
        bill.put("totalAmount", order.getTotalAmount());

        // Digital signature (in production, this should be a proper digital signature)
        bill.put("digitalSignature", generateDigitalSignature(transactionId));
        
        return bill;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateDigitalSignature(String transactionId) {
        // In production, implement proper digital signature
        return "SIG-" + UUID.randomUUID().toString().toUpperCase();
    }

    private void sendPaymentConfirmationEmail(Order order, Map<String, Object> bill) {
        String subject = "Payment Confirmation - Order #" + order.getTransactionId();
        emailService.sendOrderConfirmation(order.getUser().getEmail(), order.getTransactionId(), bill);
    }

    private void sendCodConfirmationEmail(Order order, Map<String, Object> bill) {
        String subject = "Order Confirmation - Cash on Delivery - Order #" + order.getTransactionId();
        emailService.sendOrderConfirmation(order.getUser().getEmail(), order.getTransactionId(), bill);
    }

    public BigDecimal calculateDeliveryCharge(BigDecimal orderAmount) {
        // Example delivery charge calculation
        return orderAmount.compareTo(new BigDecimal("500")) < 0 ? 
            new BigDecimal("40") : BigDecimal.ZERO;
    }

    public BigDecimal calculateTax(BigDecimal amount) {
        // Example tax calculation (5% GST)
        return amount.multiply(new BigDecimal("0.05"));
    }
}
