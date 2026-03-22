package com.example.orderservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, String>{
    
    Optional<OrderEntity> findByOrderId(String orderId);
    
    List<OrderEntity> findByStatusAndReservedAtBefore(String status, LocalDateTime reservedAt);
}
