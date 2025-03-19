package com.grocerydeliveryapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error but don't throw it to prevent blocking the main flow
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> templateModel) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            Context context = new Context();
            context.setVariables(templateModel);
            String htmlContent = templateEngine.process(templateName, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send HTML email: " + e.getMessage());
        }
    }

    @Async
    public void sendOrderConfirmation(String to, String orderNumber, Map<String, Object> orderDetails) {
        String subject = "Order Confirmation - #" + orderNumber;
        Map<String, Object> templateModel = Map.of(
            "orderNumber", orderNumber,
            "orderDetails", orderDetails,
            "supportEmail", "support@groceryapp.com",
            "supportPhone", "+1-888-GROCERY"
        );
        
        sendHtmlEmail(to, subject, "order-confirmation", templateModel);
    }

    @Async
    public void sendOrderStatusUpdate(String to, String orderNumber, String status, String estimatedDeliveryTime) {
        String subject = "Order Status Update - #" + orderNumber;
        Map<String, Object> templateModel = Map.of(
            "orderNumber", orderNumber,
            "status", status,
            "estimatedDeliveryTime", estimatedDeliveryTime,
            "trackingUrl", "https://groceryapp.com/track/" + orderNumber
        );
        
        sendHtmlEmail(to, subject, "order-status-update", templateModel);
    }

    @Async
    public void sendDeliveryConfirmation(String to, String orderNumber) {
        String subject = "Order Delivered - #" + orderNumber;
        Map<String, Object> templateModel = Map.of(
            "orderNumber", orderNumber,
            "feedbackUrl", "https://groceryapp.com/feedback/" + orderNumber
        );
        
        sendHtmlEmail(to, subject, "delivery-confirmation", templateModel);
    }

    @Async
    public void sendPasswordResetEmail(String to, String resetToken) {
        String subject = "Password Reset Request";
        Map<String, Object> templateModel = Map.of(
            "resetToken", resetToken,
            "resetUrl", "https://groceryapp.com/reset-password?token=" + resetToken,
            "expiryHours", "24"
        );
        
        sendHtmlEmail(to, subject, "password-reset", templateModel);
    }

    @Async
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to Grocery Delivery App!";
        Map<String, Object> templateModel = Map.of(
            "username", username,
            "supportEmail", "support@groceryapp.com",
            "supportPhone", "+1-888-GROCERY"
        );
        
        sendHtmlEmail(to, subject, "welcome", templateModel);
    }
}
