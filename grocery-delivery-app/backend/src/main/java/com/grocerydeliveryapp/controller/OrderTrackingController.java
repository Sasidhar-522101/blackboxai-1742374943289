package com.grocerydeliveryapp.controller;

import com.grocerydeliveryapp.dto.order.OrderResponse;
import com.grocerydeliveryapp.model.OrderStatus;
import com.grocerydeliveryapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OrderTrackingController {

    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/order.track/{orderId}")
    @SendTo("/topic/order/{orderId}")
    public OrderResponse trackOrder(@DestinationVariable Long orderId) {
        return orderService.getOrderById(orderId, null); // null for userId as this is public tracking
    }

    // Method to be called from OrderService when order status changes
    public void sendOrderStatusUpdate(Long orderId, Long userId, OrderStatus newStatus) {
        // Send to specific user's queue
        String userDestination = String.format("/queue/user/%d/orders/%d", userId, orderId);
        OrderResponse orderResponse = orderService.getOrderById(orderId, userId);
        messagingTemplate.convertAndSend(userDestination, orderResponse);

        // Send to public topic (for tracking without authentication)
        String publicDestination = String.format("/topic/order/%d", orderId);
        messagingTemplate.convertAndSend(publicDestination, orderResponse);

        // Send delivery partner updates if order is out for delivery
        if (newStatus == OrderStatus.OUT_FOR_DELIVERY) {
            sendDeliveryPartnerUpdate(orderId, orderResponse);
        }
    }

    // Method to send location updates for orders that are out for delivery
    public void sendLocationUpdate(Long orderId, Double latitude, Double longitude) {
        Map<String, Object> locationUpdate = new HashMap<>();
        locationUpdate.put("orderId", orderId);
        locationUpdate.put("latitude", latitude);
        locationUpdate.put("longitude", longitude);
        locationUpdate.put("timestamp", LocalDateTime.now());

        String destination = String.format("/topic/order/%d/location", orderId);
        messagingTemplate.convertAndSend(destination, locationUpdate);
    }

    private void sendDeliveryPartnerUpdate(Long orderId, OrderResponse orderResponse) {
        Map<String, Object> deliveryUpdate = new HashMap<>();
        deliveryUpdate.put("orderId", orderId);
        deliveryUpdate.put("estimatedDeliveryTime", orderResponse.getEstimatedDeliveryTime());
        deliveryUpdate.put("deliveryPartnerName", orderResponse.getDeliveryPartnerName());
        deliveryUpdate.put("deliveryPartnerPhone", orderResponse.getDeliveryPartnerPhone());

        String destination = String.format("/topic/order/%d/delivery", orderId);
        messagingTemplate.convertAndSend(destination, deliveryUpdate);
    }

    // Method to send order preparation updates
    public void sendPreparationUpdate(Long orderId, String message, int progressPercentage) {
        Map<String, Object> preparationUpdate = new HashMap<>();
        preparationUpdate.put("orderId", orderId);
        preparationUpdate.put("message", message);
        preparationUpdate.put("progress", progressPercentage);
        preparationUpdate.put("timestamp", LocalDateTime.now());

        String destination = String.format("/topic/order/%d/preparation", orderId);
        messagingTemplate.convertAndSend(destination, preparationUpdate);
    }

    // Method to notify about delivery delays
    public void sendDeliveryDelayNotification(Long orderId, Long userId, String reason, int delayMinutes) {
        Map<String, Object> delayUpdate = new HashMap<>();
        delayUpdate.put("orderId", orderId);
        delayUpdate.put("reason", reason);
        delayUpdate.put("delayMinutes", delayMinutes);
        delayUpdate.put("newEstimatedTime", LocalDateTime.now().plusMinutes(delayMinutes));

        // Send to specific user
        String userDestination = String.format("/queue/user/%d/orders/%d/delay", userId, orderId);
        messagingTemplate.convertAndSend(userDestination, delayUpdate);

        // Send to public topic
        String publicDestination = String.format("/topic/order/%d/delay", orderId);
        messagingTemplate.convertAndSend(publicDestination, delayUpdate);
    }

    // Method to send delivery completion confirmation
    public void sendDeliveryConfirmation(Long orderId, Long userId) {
        OrderResponse orderResponse = orderService.getOrderById(orderId, userId);
        
        Map<String, Object> deliveryConfirmation = new HashMap<>();
        deliveryConfirmation.put("orderId", orderId);
        deliveryConfirmation.put("deliveryTime", LocalDateTime.now());
        deliveryConfirmation.put("orderDetails", orderResponse);

        // Send to specific user
        String userDestination = String.format("/queue/user/%d/orders/%d/delivered", userId, orderId);
        messagingTemplate.convertAndSend(userDestination, deliveryConfirmation);

        // Send to public topic
        String publicDestination = String.format("/topic/order/%d/delivered", orderId);
        messagingTemplate.convertAndSend(publicDestination, deliveryConfirmation);
    }
}
