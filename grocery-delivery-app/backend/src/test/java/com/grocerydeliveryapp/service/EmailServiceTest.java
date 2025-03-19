package com.grocerydeliveryapp.service;

import com.grocerydeliveryapp.model.Order;
import com.grocerydeliveryapp.model.OrderStatus;
import com.grocerydeliveryapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ITemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<Context> contextCaptor;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("Processed template");

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("99.99"));
    }

    @Test
    void sendEmailSuccess() throws MessagingException {
        // Act
        emailService.sendEmail(
            "test@example.com",
            "Test Subject",
            "Test content"
        );

        // Assert
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendOtpEmailSuccess() throws MessagingException {
        // Arrange
        String otp = "123456";

        // Act
        emailService.sendOtpEmail("test@example.com", otp);

        // Assert
        verify(templateEngine).process(eq("email/otp"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        assertEquals(otp, capturedContext.getVariable("otp"));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendOrderConfirmationSuccess() throws MessagingException {
        // Arrange
        Map<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("orderId", "ORD-123");
        orderDetails.put("totalAmount", "99.99");

        // Act
        emailService.sendOrderConfirmation(
            testUser.getEmail(),
            "ORD-123",
            orderDetails
        );

        // Assert
        verify(templateEngine).process(eq("email/order-confirmation"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        assertEquals("ORD-123", capturedContext.getVariable("orderId"));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendOrderStatusUpdateSuccess() throws MessagingException {
        // Act
        emailService.sendOrderStatusUpdate(
            testUser.getEmail(),
            "ORD-123",
            OrderStatus.CONFIRMED.toString(),
            "Your order has been confirmed"
        );

        // Assert
        verify(templateEngine).process(eq("email/order-status-update"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        assertEquals("ORD-123", capturedContext.getVariable("orderId"));
        assertEquals(OrderStatus.CONFIRMED.toString(), capturedContext.getVariable("status"));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendDeliveryConfirmationSuccess() throws MessagingException {
        // Arrange
        Map<String, Object> deliveryDetails = new HashMap<>();
        deliveryDetails.put("deliveryTime", "2023-12-25 14:30");
        deliveryDetails.put("deliveryAddress", "123 Test St");

        // Act
        emailService.sendDeliveryConfirmation(
            testUser.getEmail(),
            "ORD-123",
            deliveryDetails
        );

        // Assert
        verify(templateEngine).process(eq("email/delivery-confirmation"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        assertEquals("ORD-123", capturedContext.getVariable("orderId"));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendOrderCancellationSuccess() throws MessagingException {
        // Act
        emailService.sendOrderCancellation(
            testUser.getEmail(),
            "ORD-123",
            "Order cancelled by customer"
        );

        // Assert
        verify(templateEngine).process(eq("email/order-cancellation"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        assertEquals("ORD-123", capturedContext.getVariable("orderId"));
        assertEquals("Order cancelled by customer", capturedContext.getVariable("reason"));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendPaymentConfirmationSuccess() throws MessagingException {
        // Arrange
        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("transactionId", "TXN-123");
        paymentDetails.put("amount", "99.99");
        paymentDetails.put("paymentMethod", "CARD");

        // Act
        emailService.sendPaymentConfirmation(
            testUser.getEmail(),
            "ORD-123",
            paymentDetails
        );

        // Assert
        verify(templateEngine).process(eq("email/payment-confirmation"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        assertEquals("ORD-123", capturedContext.getVariable("orderId"));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendDeliveryDelayNotificationSuccess() throws MessagingException {
        // Act
        emailService.sendDeliveryDelayNotification(
            testUser.getEmail(),
            "ORD-123",
            30,
            "Heavy traffic in the area"
        );

        // Assert
        verify(templateEngine).process(eq("email/delivery-delay"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        assertEquals("ORD-123", capturedContext.getVariable("orderId"));
        assertEquals(30, capturedContext.getVariable("delayMinutes"));
        assertEquals("Heavy traffic in the area", capturedContext.getVariable("reason"));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void handleEmailExceptionGracefully() throws MessagingException {
        // Arrange
        doThrow(new MessagingException("Failed to send email"))
            .when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertDoesNotThrow(() -> 
            emailService.sendEmail("test@example.com", "Test Subject", "Test content")
        );
    }
}
