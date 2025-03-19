package com.grocerydeliveryapp.integration;

import com.grocerydeliveryapp.dto.order.OrderResponse;
import com.grocerydeliveryapp.model.Order;
import com.grocerydeliveryapp.model.OrderStatus;
import com.grocerydeliveryapp.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderTrackingWebSocketTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private OrderRepository orderRepository;

    private WebSocketStompClient stompClient;
    private String websocketUrl;
    private Order testOrder;

    @BeforeEach
    void setup() {
        websocketUrl = "ws://localhost:" + port + "/ws";
        stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // Create test order
        testOrder = createTestOrder();
        testOrder = orderRepository.save(testOrder);
    }

    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setUser(testUser);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setDeliveryAddress("123 Test St");
        return order;
    }

    @Test
    void testOrderTracking() throws ExecutionException, InterruptedException, TimeoutException {
        // Create a future to store the received message
        CompletableFuture<OrderResponse> completableFuture = new CompletableFuture<>();

        // Connect to WebSocket
        StompSession session = stompClient.connect(websocketUrl, new WebSocketHttpHeaders(), 
            new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);

        // Subscribe to order updates
        session.subscribe("/topic/order/" + testOrder.getId(), new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return OrderResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                completableFuture.complete((OrderResponse) payload);
            }
        });

        // Send tracking request
        session.send("/app/order.track/" + testOrder.getId(), null);

        // Wait for response
        OrderResponse response = completableFuture.get(5, TimeUnit.SECONDS);

        // Assert
        assertNotNull(response);
        assertEquals(testOrder.getId(), response.getId());
        assertEquals(OrderStatus.PENDING, response.getStatus());
    }

    @Test
    void testLocationUpdates() throws ExecutionException, InterruptedException, TimeoutException {
        // Create a future to store the received location update
        CompletableFuture<Map<String, Object>> completableFuture = new CompletableFuture<>();

        // Connect to WebSocket
        StompSession session = stompClient.connect(websocketUrl, new WebSocketHttpHeaders(), 
            new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);

        // Subscribe to location updates
        session.subscribe("/topic/order/" + testOrder.getId() + "/location", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                completableFuture.complete((Map<String, Object>) payload);
            }
        });

        // Update order status to OUT_FOR_DELIVERY
        testOrder.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepository.save(testOrder);

        // Send location update through service
        orderTrackingController.sendLocationUpdate(testOrder.getId(), 12.9716, 77.5946);

        // Wait for response
        Map<String, Object> response = completableFuture.get(5, TimeUnit.SECONDS);

        // Assert
        assertNotNull(response);
        assertEquals(testOrder.getId(), response.get("orderId"));
        assertEquals(12.9716, response.get("latitude"));
        assertEquals(77.5946, response.get("longitude"));
    }

    @Test
    void testDeliveryUpdates() throws ExecutionException, InterruptedException, TimeoutException {
        // Create a future to store the received delivery update
        CompletableFuture<Map<String, Object>> completableFuture = new CompletableFuture<>();

        // Connect to WebSocket
        StompSession session = stompClient.connect(websocketUrl, new WebSocketHttpHeaders(), 
            new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);

        // Subscribe to delivery updates
        session.subscribe("/topic/order/" + testOrder.getId() + "/delivery", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                completableFuture.complete((Map<String, Object>) payload);
            }
        });

        // Update order status and send delivery update
        testOrder.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepository.save(testOrder);
        orderTrackingController.sendDeliveryPartnerUpdate(testOrder.getId(), 
            orderService.getOrderById(testOrder.getId(), testUser.getId()));

        // Wait for response
        Map<String, Object> response = completableFuture.get(5, TimeUnit.SECONDS);

        // Assert
        assertNotNull(response);
        assertEquals(testOrder.getId(), response.get("orderId"));
        assertNotNull(response.get("estimatedDeliveryTime"));
    }

    @Test
    void testPreparationUpdates() throws ExecutionException, InterruptedException, TimeoutException {
        // Create a future to store the received preparation update
        CompletableFuture<Map<String, Object>> completableFuture = new CompletableFuture<>();

        // Connect to WebSocket
        StompSession session = stompClient.connect(websocketUrl, new WebSocketHttpHeaders(), 
            new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);

        // Subscribe to preparation updates
        session.subscribe("/topic/order/" + testOrder.getId() + "/preparation", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                completableFuture.complete((Map<String, Object>) payload);
            }
        });

        // Send preparation update
        orderTrackingController.sendPreparationUpdate(testOrder.getId(), "Order is being prepared", 50);

        // Wait for response
        Map<String, Object> response = completableFuture.get(5, TimeUnit.SECONDS);

        // Assert
        assertNotNull(response);
        assertEquals(testOrder.getId(), response.get("orderId"));
        assertEquals("Order is being prepared", response.get("message"));
        assertEquals(50, response.get("progress"));
    }
}
