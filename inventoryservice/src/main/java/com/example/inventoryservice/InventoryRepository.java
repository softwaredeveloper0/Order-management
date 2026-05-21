package com.example.inventoryservice;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {
    Optional<InventoryEntity> findByProductId(String productId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update InventoryEntity i set i.reservedStock = coalesce(i.reservedStock, 0) + :qty where i.productId = :productId and coalesce(i.totalStock, 0) - coalesce(i.reservedStock, 0) >= :qty")
    int reserveStock(@Param("productId") String productId, @Param("qty") Long qty);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update InventoryEntity i set i.reservedStock = coalesce(i.reservedStock, 0) - :qty where i.productId = :productId and coalesce(i.reservedStock, 0) >= :qty")
    int releaseStock(@Param("productId") String productId, @Param("qty") Long qty);
}
