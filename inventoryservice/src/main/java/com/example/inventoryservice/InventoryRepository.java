package com.example.inventoryservice;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long>{
    Optional<InventoryEntity> findByProductId(String productId);

}
