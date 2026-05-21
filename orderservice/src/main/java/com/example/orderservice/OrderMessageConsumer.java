package com.example.orderservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class OrderMessageConsumer {

    @Autowired
    private OrderRepository orderRepository;
    

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_QUEUE)
    public void consume(OrderMessage message) {

        System.out.println("Received inventory confirmation from inventory service: " + message);
        System.out.println("Order ID: " + message.getOrderId());
        System.out.println("Product ID: " + message.getProductId());
        System.out.println("Status: " + message.getStatus());
        System.out.println("Available Stock: " + message.getStock());

        // Only process reservation confirmations
        if (message.getOrderId() == null || message.getProductId() == null) {
            System.out.println("Invalid confirmation message - missing orderId or productId");
            return;
        }

        try {
            OrderEntity order = orderRepository.findByOrderId(message.getOrderId()).orElse(null);
            if (order == null) {
                System.out.println("Order not found: " + message.getOrderId());
                return;
            }

            // Only update orders that are in RESERVED status
            if (!"RESERVED".equals(order.getStatus())) {
                System.out.println("Order " + message.getOrderId() + " is not in RESERVED status, skipping update");
                return;
            }

            // Check reservation confirmation status
            if ("RESERVATION_CONFIRMED".equals(message.getStatus())) {
                order.setStatus("CONFIRMED");
                System.out.println("Order " + message.getOrderId() + " confirmed - stock reservation successful");
            } else if ("RESERVATION_FAILED".equals(message.getStatus())) {
                order.setStatus("PENDING");
                System.out.println("Order " + message.getOrderId() + " set to pending - insufficient stock");
            }

            orderRepository.save(order);
            System.out.println("Order " + message.getOrderId() + " status updated to: " + order.getStatus());
        } catch (Exception e) {
            System.err.println("Error processing inventory confirmation for order " + message.getOrderId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}