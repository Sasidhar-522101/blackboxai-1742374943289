package com.grocerydeliveryapp.service;

import com.grocerydeliveryapp.dto.order.OrderItemRequest;
import com.grocerydeliveryapp.dto.order.OrderRequest;
import com.grocerydeliveryapp.dto.order.OrderResponse;
import com.grocerydeliveryapp.model.*;
import com.grocerydeliveryapp.repository.OrderRepository;
import com.grocerydeliveryapp.repository.ProductRepository;
import com.grocerydeliveryapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final EmailService emailService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderItems(new ArrayList<>());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setDeliveryInstructions(request.getDeliveryInstructions());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus("PENDING");
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusHours(2));
        order.setCreatedAt(LocalDateTime.now());
        
        // Process order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemRequest.getProductId()));

            if (!product.isAvailable() || product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalStateException("Product " + product.getName() + " is not available in requested quantity");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtTime(product.getPrice());
            orderItem.setDiscountAtTime(product.getDiscountPercentage() != null ? 
                product.getPrice().multiply(BigDecimal.valueOf(product.getDiscountPercentage() / 100)) : 
                BigDecimal.ZERO);

            order.getOrderItems().add(orderItem);
            
            // Update product stock
            productService.updateStock(product.getId(), itemRequest.getQuantity());
            
            // Calculate total
            orderItem.calculateSubtotal();
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        // Set delivery charge (example: ₹40 for orders below ₹500)
        BigDecimal deliveryCharge = totalAmount.compareTo(new BigDecimal("500")) < 0 ? 
            new BigDecimal("40") : BigDecimal.ZERO;
        order.setDeliveryCharge(deliveryCharge);

        // Calculate tax (example: 5% GST)
        BigDecimal taxAmount = totalAmount.multiply(new BigDecimal("0.05"));
        order.setTaxAmount(taxAmount);

        // Set total amount including delivery charge and tax
        order.setTotalAmount(totalAmount.add(deliveryCharge).add(taxAmount));

        // Generate unique order number
        String orderNumber = generateOrderNumber();
        order.setTransactionId(orderNumber);

        // Save the order
        Order savedOrder = orderRepository.save(order);

        // Send order confirmation email
        sendOrderConfirmationEmail(savedOrder);

        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Unauthorized access to order");
        }

        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        return orderRepository.findByUser(user, pageable)
                .map(this::mapToOrderResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        if (newStatus == OrderStatus.OUT_FOR_DELIVERY) {
            order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(45));
        } else if (newStatus == OrderStatus.DELIVERED) {
            order.setActualDeliveryTime(LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);
        
        // Send status update email
        sendOrderStatusUpdateEmail(updatedOrder);

        return mapToOrderResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Unauthorized access to order");
        }

        if (!order.canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        // Restore product stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            product.setAvailable(true);
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order cancelledOrder = orderRepository.save(order);
        
        // Send cancellation email
        sendOrderCancellationEmail(cancelledOrder);

        return mapToOrderResponse(cancelledOrder);
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void sendOrderConfirmationEmail(Order order) {
        emailService.sendOrderConfirmation(
            order.getUser().getEmail(),
            order.getTransactionId(),
            createOrderDetailsMap(order)
        );
    }

    private void sendOrderStatusUpdateEmail(Order order) {
        emailService.sendOrderStatusUpdate(
            order.getUser().getEmail(),
            order.getTransactionId(),
            order.getStatus().toString(),
            order.getEstimatedDeliveryTime().toString()
        );
    }

    private void sendOrderCancellationEmail(Order order) {
        String subject = "Order Cancelled - #" + order.getTransactionId();
        String body = String.format(
            "Your order #%s has been cancelled. If you paid for this order, a refund will be processed shortly.",
            order.getTransactionId()
        );
        emailService.sendEmail(order.getUser().getEmail(), subject, body);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productImage(item.getProduct().getImageUrl())
                        .quantity(item.getQuantity())
                        .priceAtTime(item.getPriceAtTime())
                        .discountAtTime(item.getDiscountAtTime())
                        .subtotal(item.getSubtotal())
                        .unit(item.getProduct().getUnit())
                        .build())
                .collect(Collectors.toList());

        List<OrderResponse.OrderTrackingEvent> trackingEvents = new ArrayList<>();
        // Add tracking events based on order status history
        trackingEvents.add(new OrderResponse.OrderTrackingEvent(
            order.getCreatedAt(),
            "ORDER_PLACED",
            "Order has been placed successfully",
            order.getDeliveryAddress()
        ));

        if (order.getStatus() != OrderStatus.PENDING) {
            trackingEvents.add(new OrderResponse.OrderTrackingEvent(
                order.getUpdatedAt(),
                order.getStatus().toString(),
                getStatusDescription(order.getStatus()),
                order.getDeliveryAddress()
            ));
        }

        OrderResponse response = OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getTransactionId())
                .orderItems(itemResponses)
                .subtotal(order.getTotalAmount().subtract(order.getDeliveryCharge()).subtract(order.getTaxAmount()))
                .deliveryCharge(order.getDeliveryCharge())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryInstructions(order.getDeliveryInstructions())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .transactionId(order.getTransactionId())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .actualDeliveryTime(order.getActualDeliveryTime())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .customerName(order.getUser().getUsername())
                .customerEmail(order.getUser().getEmail())
                .customerPhone(order.getUser().getPhoneNumber())
                .trackingEvents(trackingEvents)
                .build();

        response.calculateDeliveryProgress();
        response.generateStatusDescription();
        
        return response;
    }

    private String getStatusDescription(OrderStatus status) {
        switch (status) {
            case CONFIRMED:
                return "Order has been confirmed by the store";
            case PREPARING:
                return "Your order is being prepared";
            case OUT_FOR_DELIVERY:
                return "Order is out for delivery";
            case DELIVERED:
                return "Order has been delivered successfully";
            case CANCELLED:
                return "Order has been cancelled";
            case REFUNDED:
                return "Order has been refunded";
            default:
                return "Status unknown";
        }
    }

    private java.util.Map<String, Object> createOrderDetailsMap(Order order) {
        return java.util.Map.of(
            "orderNumber", order.getTransactionId(),
            "totalAmount", order.getTotalAmount().toString(),
            "deliveryAddress", order.getDeliveryAddress(),
            "estimatedDelivery", order.getEstimatedDeliveryTime().toString(),
            "itemCount", order.getOrderItems().size()
        );
    }
}
