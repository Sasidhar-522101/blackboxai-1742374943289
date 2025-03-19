package com.grocerydeliveryapp.integration;

import com.grocerydeliveryapp.dto.order.OrderItemRequest;
import com.grocerydeliveryapp.dto.order.OrderRequest;
import com.grocerydeliveryapp.dto.order.OrderResponse;
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
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class OrderControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;
    private Order testOrder;
    private OrderRequest orderRequest;

    @BeforeEach
    void setupTestData() {
        // Create test product
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(100);
        testProduct.setAvailable(true);
        testProduct = productRepository.save(testProduct);

        // Create order request
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(testProduct.getId());
        itemRequest.setQuantity(2);

        orderRequest = new OrderRequest();
        orderRequest.setOrderItems(Collections.singletonList(itemRequest));
        orderRequest.setDeliveryAddress("123 Test St");
        orderRequest.setDeliveryInstructions("Leave at door");
        orderRequest.setPaymentMethod("CARD");

        // Create test order
        testOrder = orderRepository.save(createTestOrder());
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setUser(testUser);
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress("123 Test St");
        order.setPaymentMethod("CARD");
        order.setTotalAmount(new BigDecimal("199.98"));
        return order;
    }

    @Test
    void createOrderSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(post("/api/orders")
                .header("Authorization", getAuthHeader(false))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        OrderResponse response = fromJson(result.getResponse().getContentAsString(), OrderResponse.class);
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(orderRequest.getDeliveryAddress(), response.getDeliveryAddress());
        assertEquals(2, response.getOrderItems().size());
    }

    @Test
    void createOrderUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrderByIdSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(get("/api/orders/{orderId}", testOrder.getId())
                .header("Authorization", getAuthHeader(false)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        OrderResponse response = fromJson(result.getResponse().getContentAsString(), OrderResponse.class);
        assertNotNull(response);
        assertEquals(testOrder.getId(), response.getId());
        assertEquals(testOrder.getStatus(), response.getStatus());
    }

    @Test
    void getOrderByIdUnauthorized() throws Exception {
        // Act & Assert - Different user trying to access order
        mockMvc.perform(get("/api/orders/{orderId}", testOrder.getId())
                .header("Authorization", getAuthHeader(true))) // Using admin token
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserOrdersSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(get("/api/orders/user")
                .header("Authorization", getAuthHeader(false))
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains(testOrder.getId().toString()));
    }

    @Test
    void cancelOrderSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(post("/api/orders/{orderId}/cancel", testOrder.getId())
                .header("Authorization", getAuthHeader(false)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        OrderResponse response = fromJson(result.getResponse().getContentAsString(), OrderResponse.class);
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
    }

    @Test
    void updateOrderStatusAdminSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(put("/api/orders/{orderId}/status", testOrder.getId())
                .header("Authorization", getAuthHeader(true))
                .param("status", OrderStatus.CONFIRMED.toString()))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        OrderResponse response = fromJson(result.getResponse().getContentAsString(), OrderResponse.class);
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
    }

    @Test
    void updateOrderStatusUnauthorized() throws Exception {
        // Act & Assert - Regular user trying to update status
        mockMvc.perform(put("/api/orders/{orderId}/status", testOrder.getId())
                .header("Authorization", getAuthHeader(false))
                .param("status", OrderStatus.CONFIRMED.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPendingOrdersAdminSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(get("/api/orders/admin/pending")
                .header("Authorization", getAuthHeader(true))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains(testOrder.getId().toString()));
    }

    @Test
    void getOrdersForDeliveryTodayAdminSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(get("/api/orders/admin/delivery-today")
                .header("Authorization", getAuthHeader(true))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert response structure
        assertTrue(result.getResponse().getContentAsString().contains("content"));
    }

    @Test
    void getDelayedOrdersAdminSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(get("/api/orders/admin/delayed")
                .header("Authorization", getAuthHeader(true))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert response structure
        assertTrue(result.getResponse().getContentAsString().contains("content"));
    }

    @Test
    void getOrderStatisticsAdminSuccess() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(get("/api/orders/admin/statistics")
                .header("Authorization", getAuthHeader(true)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert response is not empty
        assertTrue(result.getResponse().getContentLength() > 0);
    }
}
