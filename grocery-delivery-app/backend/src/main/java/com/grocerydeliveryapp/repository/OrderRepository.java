package com.grocerydeliveryapp.repository;

import com.grocerydeliveryapp.model.Order;
import com.grocerydeliveryapp.model.OrderStatus;
import com.grocerydeliveryapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by user
    Page<Order> findByUser(User user, Pageable pageable);
    
    // Find orders by status
    List<Order> findByStatus(OrderStatus status);
    
    // Find orders by user and status
    List<Order> findByUserAndStatus(User user, OrderStatus status);
    
    // Find orders created between dates
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find orders by payment method
    List<Order> findByPaymentMethod(String paymentMethod);
    
    // Find orders with specific payment status
    List<Order> findByPaymentStatus(String paymentStatus);
    
    // Find recent orders by user
    @Query("SELECT o FROM Order o WHERE o.user = :user ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByUser(@Param("user") User user, Pageable pageable);
    
    // Count orders by status
    Long countByStatus(OrderStatus status);
    
    // Find orders that need delivery today
    @Query("SELECT o FROM Order o WHERE DATE(o.estimatedDeliveryTime) = CURRENT_DATE AND " +
           "o.status NOT IN ('DELIVERED', 'CANCELLED', 'REFUNDED')")
    List<Order> findOrdersForDeliveryToday();
    
    // Find delayed orders
    @Query("SELECT o FROM Order o WHERE o.estimatedDeliveryTime < CURRENT_TIMESTAMP AND " +
           "o.status NOT IN ('DELIVERED', 'CANCELLED', 'REFUNDED')")
    List<Order> findDelayedOrders();
    
    // Find orders by delivery partner
    List<Order> findByDeliveryPartnerName(String deliveryPartnerName);
    
    // Find unrated delivered orders
    List<Order> findByStatusAndIsRatedFalse(OrderStatus status);
    
    // Find orders with high ratings
    List<Order> findByRatingGreaterThanEqual(Integer rating);
    
    // Calculate average rating for delivered orders
    @Query("SELECT AVG(o.rating) FROM Order o WHERE o.status = 'DELIVERED' AND o.rating IS NOT NULL")
    Double calculateAverageRating();
    
    // Find orders requiring attention (delayed or with issues)
    @Query("SELECT o FROM Order o WHERE " +
           "(o.estimatedDeliveryTime < CURRENT_TIMESTAMP AND o.status NOT IN ('DELIVERED', 'CANCELLED', 'REFUNDED')) OR " +
           "(o.status = 'DELIVERED' AND o.rating <= 2)")
    List<Order> findOrdersRequiringAttention();
    
    // Find orders by multiple statuses
    List<Order> findByStatusIn(List<OrderStatus> statuses);
}
