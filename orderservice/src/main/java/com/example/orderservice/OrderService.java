package com.example.orderservice;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    
    
    private final OrderRepository orderRepository;
    
    private final OrderMessageProducer orderMessageProducer;
    
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository,OrderMessageProducer orderMessageProducer,ProductClient productClient){
        this.orderRepository = orderRepository;
        this.orderMessageProducer = orderMessageProducer;
        this.productClient = productClient;
    }
    

    public ResponseEntity<?> CreateOrder(OrderEntity order){
        
        // Validate order data
        if (order.getProductId() == null || order.getProductId().isEmpty()) {
            return ResponseEntity.badRequest().body("Product ID is required");
        }
        
        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("Quantity must be greater than 0");
        }
        
        if (order.getPrice() == null || order.getPrice() <= 0) {
            return ResponseEntity.badRequest().body("Price is required and must be greater than 0");
        }

        // Fetch product from product service to validate price
        try {
            ResponseEntity<ProductResponse> response = productClient.getProductByProductId(order.getProductId());
            
            if (response == null || !response.hasBody() || response.getBody() == null) {
                return ResponseEntity.badRequest().body("Product not found with ID: " + order.getProductId());
            }
            
            ProductResponse product = response.getBody();
            
            // Additional null check for safety
            if (product == null || product.getPrice() == null) {
                return ResponseEntity.badRequest().body("Product price information is missing.");
            }
            
            // Validate price - compare with actual product price
            double expectedPrice = product.getPrice() * order.getQuantity();
            double providedPrice = order.getPrice();
            
            // Allow small floating point differences (0.01)
            if (Math.abs(expectedPrice - providedPrice) > 0.01) {
                return ResponseEntity.badRequest().body(
                    String.format("Price mismatch. Expected: %.2f (%.2f x %d), Provided: %.2f", 
                        expectedPrice, product.getPrice(), order.getQuantity(), providedPrice)
                );
            }
        } catch (Exception e) {
            System.err.println("Error fetching product: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error validating product: " + e.getMessage());
        }



        // Set initial order status, timestamps, and reservation time
        order.setStatus("RESERVED");
        order.setCreatedAt(LocalDateTime.now());
        order.setReservedAt(LocalDateTime.now()); // Set reservation timestamp
        
        // Save the order first
        OrderEntity savedOrder = orderRepository.save(order);
        
        // Create order message to send to inventory service for reservation
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOrderId(savedOrder.getOrderId());
        orderMessage.setProductId(savedOrder.getProductId());
        orderMessage.setStock(savedOrder.getQuantity()); // stock field represents quantity
        orderMessage.setPrice(savedOrder.getPrice());
        orderMessage.setReservedStock(savedOrder.getQuantity());

        orderMessage.setStatus("RESERVED");
        
        // Send order message to inventory service to reserve stock
        orderMessageProducer.sendOrderUpdate(orderMessage);
        
        System.out.println("Order created with ID: " + savedOrder.getOrderId() + 
            " and reserved. Payment must be completed within 5 minutes.");
        
        return ResponseEntity.ok("Order created successfully. Status: RESERVED - Payment must be completed within 5 minutes");
    }
}
