package com.example.orderservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, String>{
    
    Optional<OrderEntity> findByOrderId(String orderId);
    
    List<OrderEntity> findByStatusAndReservedAtBefore(String status, LocalDateTime reservedAt);

    @Modifying
    @Transactional
    @Query("delete from OrderEntity o where o.status = 'CANCELLED'")
    int deleteAllCancelledOrders();

    @Modifying
    @Transactional
    @Query("delete from OrderEntity o where o.status = 'CANCELLED' and o.reservedAt <= :threshold")
    int deleteCancelledOrdersOlderThan(LocalDateTime threshold);
}

