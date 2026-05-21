package com.example.orderservice;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class OrderService {
    
    
    private final OrderRepository orderRepository;
    
    private final ProductClient productClient;

    private final InventoryClient inventoryClient;

    public OrderService(OrderRepository orderRepository, ProductClient productClient, InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
    }
    
    @Transactional
    public ResponseEntity<?> CreateOrder(OrderEntity order){
        
        // Validate order data
        if (order.getProductId() == null || order.getProductId().isEmpty()) {
            return ResponseEntity.badRequest().body("Product ID is required");
        }
        
        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("Quantity must be greater than 0");
        }
        if (order.getPrice() == null || order.getPrice() <= 0) {
            return ResponseEntity.badRequest().body("Price must be greater than 0");
        }

        // Fetch product from product service to validate price
        try {
            ResponseEntity<ProductResponse> response = productClient.getProductByProductId(order.getProductId());
            
            if (response == null || !response.hasBody() || response.getBody() == null) {
                return ResponseEntity.badRequest().body("Product not found with ID: " + order.getProductId());
            }
            
            ProductResponse product = response.getBody();
            if (product.getProductId() != null && !product.getProductId().equals(order.getProductId())) {
                return ResponseEntity.badRequest().body("Product ID mismatch returned from product service");
            }
            
            // Additional null check for safety
            if (product == null || product.getPrice() == null) {
                return ResponseEntity.badRequest().body("Product price information is missing");
            }

            // Fetch inventory from inventory service and validate stock
            ResponseEntity<InventoryResponse> inventoryResponse = inventoryClient.getInventoryByProductId(order.getProductId());
            if (inventoryResponse == null || !inventoryResponse.hasBody() || inventoryResponse.getBody() == null) {
                return ResponseEntity.badRequest().body("Inventory information not available for product ID: " + order.getProductId());
            }

            InventoryResponse inventory = inventoryResponse.getBody();
            if (inventory.getProductId() == null) {
                return ResponseEntity.badRequest().body("Inventory not found for product ID: " + order.getProductId());
            }

            long totalStock = inventory.getTotalStock() == null ? 0L : inventory.getTotalStock();
            long reservedStock = inventory.getReservedStock() == null ? 0L : inventory.getReservedStock();
            long availableStock = totalStock - reservedStock;

            if (order.getQuantity() > availableStock) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Insufficient stock for product ID: " + order.getProductId());
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

            InventoryRequest reserveRequest = new InventoryRequest();
            reserveRequest.setProductId(order.getProductId());
            reserveRequest.setStock(order.getQuantity());

            ResponseEntity<String> reserveResponse = inventoryClient.reserveInventory(reserveRequest);
            if (reserveResponse == null || reserveResponse.getStatusCode() != HttpStatus.OK) {
                String body = reserveResponse == null ? "Inventory reservation failed" : reserveResponse.getBody();
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(body == null ? "Insufficient stock for product ID: " + order.getProductId() : body);
            }
        } catch (Exception e) {
            System.err.println("Error fetching product: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error validating product: " + e.getMessage());
        }



        // Set initial order status, timestamps, and reservation time
        order.setStatus("RESERVED");
        order.setCreatedAt(LocalDateTime.now());
        order.setReservedAt(LocalDateTime.now()); // Set reservation timestamp
        
        
        OrderEntity savedOrder = orderRepository.save(order);
        
        System.out.println("Order created with ID: " + savedOrder.getOrderId() + 
            " and reserved. Payment must be completed within 5 minutes.");
        
        return ResponseEntity.ok("Order created successfully. Status: RESERVED - Payment must be completed within 5 minutes");
    }
}
