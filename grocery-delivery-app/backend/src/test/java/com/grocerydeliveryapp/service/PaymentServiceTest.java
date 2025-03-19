package com.grocerydeliveryapp.service;

import com.grocerydeliveryapp.exception.PaymentException;
import com.grocerydeliveryapp.model.Order;
import com.grocerydeliveryapp.model.OrderItem;
import com.grocerydeliveryapp.model.Product;
import com.grocerydeliveryapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PaymentService paymentService;

    private Order testOrder;
    private Map<String, String> cardPaymentDetails;
    private Map<String, String> upiPaymentDetails;

    @BeforeEach
    void setUp() {
        // Setup test user
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        // Setup test product
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));

        // Setup test order item
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPriceAtTime(product.getPrice());
        orderItem.setDiscountAtTime(BigDecimal.ZERO);

        // Setup test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(user);
        testOrder.setOrderItems(Collections.singletonList(orderItem));
        testOrder.setTotalAmount(new BigDecimal("199.98"));
        testOrder.setDeliveryCharge(new BigDecimal("40.00"));
        testOrder.setTaxAmount(new BigDecimal("10.00"));

        // Setup card payment details
        cardPaymentDetails = new HashMap<>();
        cardPaymentDetails.put("cardNumber", "4111111111111111");
        cardPaymentDetails.put("expiryMonth", "12");
        cardPaymentDetails.put("expiryYear", "2025");
        cardPaymentDetails.put("cvv", "123");

        // Setup UPI payment details
        upiPaymentDetails = new HashMap<>();
        upiPaymentDetails.put("upiId", "user@upi");
    }

    @Test
    void processCardPaymentSuccess() {
        // Act
        Map<String, Object> result = paymentService.processPayment(testOrder, "CARD", cardPaymentDetails);

        // Assert
        assertNotNull(result);
        assertNotNull(result.get("transactionId"));
        assertEquals("CARD", result.get("paymentMethod"));
        verify(emailService, times(1)).sendOrderConfirmation(anyString(), anyString(), anyMap());
    }

    @Test
    void processCardPaymentInvalidCard() {
        // Arrange
        cardPaymentDetails.put("cardNumber", "1234");

        // Act & Assert
        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentService.processPayment(testOrder, "CARD", cardPaymentDetails);
        });

        assertEquals(PaymentException.PaymentErrorType.INVALID_CARD, exception.getErrorType());
        verify(emailService, never()).sendOrderConfirmation(anyString(), anyString(), anyMap());
    }

    @Test
    void processUpiPaymentSuccess() {
        // Act
        Map<String, Object> result = paymentService.processPayment(testOrder, "UPI", upiPaymentDetails);

        // Assert
        assertNotNull(result);
        assertNotNull(result.get("transactionId"));
        assertEquals("UPI", result.get("paymentMethod"));
        verify(emailService, times(1)).sendOrderConfirmation(anyString(), anyString(), anyMap());
    }

    @Test
    void processUpiPaymentInvalidUpi() {
        // Arrange
        upiPaymentDetails.put("upiId", "invalid-upi");

        // Act & Assert
        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentService.processPayment(testOrder, "UPI", upiPaymentDetails);
        });

        assertEquals(PaymentException.PaymentErrorType.INVALID_UPI, exception.getErrorType());
        verify(emailService, never()).sendOrderConfirmation(anyString(), anyString(), anyMap());
    }

    @Test
    void processCodPaymentSuccess() {
        // Act
        Map<String, Object> result = paymentService.processPayment(testOrder, "COD", new HashMap<>());

        // Assert
        assertNotNull(result);
        assertNotNull(result.get("transactionId"));
        assertEquals("COD", result.get("paymentMethod"));
        verify(emailService, times(1)).sendOrderConfirmation(anyString(), anyString(), anyMap());
    }

    @Test
    void processPaymentUnsupportedMethod() {
        // Act & Assert
        assertThrows(PaymentException.class, () -> {
            paymentService.processPayment(testOrder, "INVALID_METHOD", new HashMap<>());
        });
        verify(emailService, never()).sendOrderConfirmation(anyString(), anyString(), anyMap());
    }

    @Test
    void calculateDeliveryChargeAboveThreshold() {
        // Act
        BigDecimal charge = paymentService.calculateDeliveryCharge(new BigDecimal("600"));

        // Assert
        assertEquals(BigDecimal.ZERO, charge);
    }

    @Test
    void calculateDeliveryChargeBelowThreshold() {
        // Act
        BigDecimal charge = paymentService.calculateDeliveryCharge(new BigDecimal("400"));

        // Assert
        assertEquals(new BigDecimal("40"), charge);
    }

    @Test
    void calculateTax() {
        // Act
        BigDecimal tax = paymentService.calculateTax(new BigDecimal("100"));

        // Assert
        assertEquals(new BigDecimal("5.00"), tax.stripTrailingZeros());
    }
}
