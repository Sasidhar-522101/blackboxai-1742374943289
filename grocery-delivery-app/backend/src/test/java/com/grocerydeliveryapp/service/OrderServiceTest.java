package com.grocerydeliveryapp.service;

import com.grocerydeliveryapp.dto.order.OrderItemRequest;
import com.grocerydeliveryapp.dto.order.OrderRequest;
import com.grocerydeliveryapp.dto.order.OrderResponse;
import com.grocerydeliveryapp.exception.OutOfStockException;
import com.grocerydeliveryapp.model.*;
import com.grocerydeliveryapp.repository.OrderRepository;
import com.grocerydeliveryapp.repository.ProductRepository;
import com.grocerydeliveryapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductService productService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private Order testOrder;
    private OrderRequest orderRequest;
    private OrderItemRequest orderItemRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Setup test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(100);
        testProduct.setAvailable(true);

        // Setup order item request
        orderItemRequest = new OrderItemRequest();
        orderItemRequest.setProductId(1L);
        orderItemRequest.setQuantity(2);

        // Setup order request
        orderRequest = new OrderRequest();
        orderRequest.setOrderItems(Collections.singletonList(orderItemRequest));
        orderRequest.setDeliveryAddress("123 Test St");
        orderRequest.setPaymentMethod("CARD");

        // Setup test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setDeliveryAddress("123 Test St");
        
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(2);
        orderItem.setPriceAtTime(testProduct.getPrice());
        testOrder.setOrderItems(Collections.singletonList(orderItem));
    }

    @Test
    void createOrderSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse response = orderService.createOrder(orderRequest, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(testUser.getUsername(), response.getCustomerName());
        verify(productService, times(1)).updateStock(anyLong(), anyInt());
        verify(emailService, times(1)).sendOrderConfirmation(anyString(), anyString(), anyMap());
    }

    @Test
    void createOrderProductNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            orderService.createOrder(orderRequest, 1L);
        });
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderOutOfStock() {
        // Arrange
        testProduct.setStockQuantity(1); // Set stock less than requested quantity
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            orderService.createOrder(orderRequest, 1L);
        });
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderByIdSuccess() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        OrderResponse response = orderService.getOrderById(1L, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(testOrder.getId(), response.getId());
        assertEquals(testOrder.getStatus(), response.getStatus());
    }

    @Test
    void getOrderByIdUnauthorized() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            orderService.getOrderById(1L, 2L); // Different user ID
        });
    }

    @Test
    void getUserOrdersSuccess() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(testOrder));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUser(any(User.class), any(Pageable.class))).thenReturn(orderPage);

        // Act
        Page<OrderResponse> response = orderService.getUserOrders(1L, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(testOrder.getId(), response.getContent().get(0).getId());
    }

    @Test
    void updateOrderStatusSuccess() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        verify(emailService, times(1)).sendOrderStatusUpdate(
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void cancelOrderSuccess() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse response = orderService.cancelOrder(1L, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void cancelOrderUnauthorized() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            orderService.cancelOrder(1L, 2L); // Different user ID
        });
        verify(orderRepository, never()).save(any(Order.class));
    }
}
