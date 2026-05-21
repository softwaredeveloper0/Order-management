package com.example.orderservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReservationScheduler {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderMessageProducer orderMessageProducer;

    // Run every minute to check for expired reservations
    @Scheduled(fixedRate = 60000) // 60000 milliseconds = 1 minute
    public void checkExpiredReservations() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        
        // Find all orders that are RESERVED and reserved more than 5 minutes ago
        List<OrderEntity> expiredOrders = orderRepository.findByStatusAndReservedAtBefore("RESERVED", fiveMinutesAgo);
        
        if (!expiredOrders.isEmpty()) {
            System.out.println("Found " + expiredOrders.size() + " expired reservation(s)");
            
            for (OrderEntity expiredOrder : expiredOrders) {
                cancelExpiredReservation(expiredOrder);
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    public void removeOldCancelledOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        int deleted = orderRepository.deleteCancelledOrdersOlderThan(threshold);
        if (deleted > 0) {
            System.out.println("Deleted " + deleted + " cancelled order(s) older than 5 minutes.");
        }
    }
    
    private void cancelExpiredReservation(OrderEntity order) {
        System.out.println("Cancelling expired reservation for order: " + order.getOrderId());
        
        // Update order status to CANCELLED
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        
        // Create message to release reserved stock back to inventory
        OrderMessage releaseMessage = new OrderMessage();
        releaseMessage.setOrderId(order.getOrderId());
        releaseMessage.setProductId(order.getProductId());
        releaseMessage.setStock(-order.getQuantity()); // Negative to release stock
        releaseMessage.setStatus("CANCELLED");
        
        // Send message to inventory service to release reserved stock
        orderMessageProducer.sendOrderUpdate(releaseMessage);
        
        System.out.println("Released " + order.getQuantity() + " units of stock for product: " + order.getProductId());
    }
}

