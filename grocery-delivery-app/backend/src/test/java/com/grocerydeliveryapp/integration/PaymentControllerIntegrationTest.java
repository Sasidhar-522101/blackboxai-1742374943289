package com.grocerydeliveryapp.integration;

import com.grocerydeliveryapp.model.Order;
import com.grocerydeliveryapp.model.OrderStatus;
import com.grocerydeliveryapp.model.Product;
import com.grocerydeliveryapp.repository.OrderRepository;
import com.grocerydeliveryapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class PaymentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private Order testOrder;
    private Map<String, String> cardPaymentDetails;
    private Map<String, String> upiPaymentDetails;

    @BeforeEach
    void setupTestData() {
        // Create test order
        testOrder = createTestOrder();
        testOrder = orderRepository.save(testOrder);

        // Setup card payment details
        cardPaymentDetails = new HashMap<>();
        cardPaymentDetails.put("cardNumber", "4111111111111111");
        cardPaymentDetails.put("expiryMonth", "12");
        cardPaymentDetails.put("expiryYear", "2025");
        cardPaymentDetails.put("cvv", "123");

        // Setup UPI payment details
        upiPaymentDetails = new HashMap<>();
        upiPaymentDetails.put("upiId", "test@upi");
    }

    private Order createTestOrder() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(100);
        product = productRepository.save(product);

        Order order = new Order();
        order.setUser(testUser);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setDeliveryCharge(new BigDecimal("40.00"));
        order.setTaxAmount(new BigDecimal("10.00"));
        order.setPaymentStatus("PENDING");
        return order;
    }

    @Test
    void processCardPaymentSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(post("/api/payments/process/{orderId}", testOrder.getId())
                .header("Authorization", getAuthHeader(false))
                .param("paymentMethod", "CARD")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(cardPaymentDetails)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        Map<String, Object> response = fromJson(result.getResponse().getContentAsString(), Map.class);
        assertNotNull(response.get("transactionId"));
        assertEquals("CARD", response.get("paymentMethod"));
    }

    @Test
    void processCardPaymentInvalidCard() throws Exception {
        // Arrange
        cardPaymentDetails.put("cardNumber", "1234");

        // Act & Assert
        mockMvc.perform(post("/api/payments/process/{orderId}", testOrder.getId())
                .header("Authorization", getAuthHeader(false))
                .param("paymentMethod", "CARD")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(cardPaymentDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value("INVALID_CARD"));
    }

    @Test
    void processUpiPaymentSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(post("/api/payments/process/{orderId}", testOrder.getId())
                .header("Authorization", getAuthHeader(false))
                .param("paymentMethod", "UPI")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(upiPaymentDetails)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        Map<String, Object> response = fromJson(result.getResponse().getContentAsString(), Map.class);
        assertNotNull(response.get("transactionId"));
        assertEquals("UPI", response.get("paymentMethod"));
    }

    @Test
    void processUpiPaymentInvalidUpi() throws Exception {
        // Arrange
        upiPaymentDetails.put("upiId", "invalid-upi");

        // Act & Assert
        mockMvc.perform(post("/api/payments/process/{orderId}", testOrder.getId())
                .header("Authorization", getAuthHeader(false))
                .param("paymentMethod", "UPI")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(upiPaymentDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value("INVALID_UPI"));
    }

    @Test
    void processCodPaymentSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(post("/api/payments/process/{orderId}", testOrder.getId())
                .header("Authorization", getAuthHeader(false))
                .param("paymentMethod", "COD"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        Map<String, Object> response = fromJson(result.getResponse().getContentAsString(), Map.class);
        assertNotNull(response.get("transactionId"));
        assertEquals("COD", response.get("paymentMethod"));
    }

    @Test
    void verifyUpiIdSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(post("/api/payments/verify-upi")
                .param("upiId", "valid@upi"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        Boolean isValid = Boolean.valueOf(result.getResponse().getContentAsString());
        assertTrue(isValid);
    }

    @Test
    void verifyUpiIdInvalid() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(post("/api/payments/verify-upi")
                .param("upiId", "invalid-upi"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        Boolean isValid = Boolean.valueOf(result.getResponse().getContentAsString());
        assertFalse(isValid);
    }

    @Test
    void validateCardSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(post("/api/payments/validate-card")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(cardPaymentDetails)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        Map<String, Object> response = fromJson(result.getResponse().getContentAsString(), Map.class);
        assertTrue((Boolean) response.get("valid"));
        assertEquals("Visa", response.get("cardType"));
    }

    @Test
    void getDigitalBillSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(get("/api/payments/order/{orderId}/bill", testOrder.getId())
                .header("Authorization", getAuthHeader(false)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        Map<String, Object> bill = fromJson(result.getResponse().getContentAsString(), Map.class);
        assertNotNull(bill.get("billNumber"));
        assertNotNull(bill.get("orderNumber"));
        assertEquals(testUser.getUsername(), bill.get("customerName"));
    }

    @Test
    void getDigitalBillUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/payments/order/{orderId}/bill", testOrder.getId()))
                .andExpect(status().isUnauthorized());
    }
}
