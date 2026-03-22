package com.example.orderservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import java.util.List;



@Service
@RequiredArgsConstructor
public class OrderMessageConsumer {

    @Autowired
    private OrderRepository orderRepository;
    

    @RabbitListener(queues = "orderQueue")
    public void consume(OrderMessage message) {

        System.out.println("Received message from inventory service: " + message);
        System.out.println("Product ID: " + message.getProductId());
        System.out.println("Stock: " + message.getStock());

        // Update orders for this product based on stock availability
        if (message.getProductId() != null && message.getStock() != null) {
            List<OrderEntity> orders = orderRepository.findAll();
            
            for (OrderEntity order : orders) {
                if (order.getProductId() != null && order.getProductId().equals(message.getProductId())) {
                    // Update order status based on stock availability
                    if (message.getStock() >= order.getQuantity()) {
                        order.setStatus("CONFIRMED");
                        System.out.println("Order " + order.getOrderId() + " confirmed - sufficient stock available");
                    } else {
                        order.setStatus("PENDING");
                        System.out.println("Order " + order.getOrderId() + " pending - insufficient stock");
                    }
                    orderRepository.save(order);
                }
            }
        }

        System.out.println("Order status updated based on inventory information");
    }
    
}