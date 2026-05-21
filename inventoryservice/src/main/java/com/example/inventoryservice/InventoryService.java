package com.example.inventoryservice;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final OrderMessageProducer orderMessageProducer;

    public InventoryService(InventoryRepository inventoryRepository,
                            OrderMessageProducer orderMessageProducer) {
        this.inventoryRepository = inventoryRepository;
        this.orderMessageProducer = orderMessageProducer;
    }

    @Transactional
    public ResponseEntity<?> UpdateInventory(String productId, Long buyingStock) {
        try {
            if (productId == null || productId.isBlank()) {
                return ResponseEntity.badRequest().body("Product id must not be null or blank");
            }
            if (buyingStock == null) {
                return ResponseEntity.badRequest().body("Buying stock must not be null");
            }
            if (buyingStock < 0) {
                return ResponseEntity.badRequest().body("Updated stock cannot be less than 0");
            }

            InventoryEntity product = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found for id: " + productId));

            Long totalStock = product.getTotalStock();
            long total = totalStock == null ? 0L : totalStock;
            Long reservedStock = product.getReservedStock();
            long reserved = reservedStock == null ? 0L : reservedStock;
            long available = total - reserved;

            if (buyingStock > available) {
                return ResponseEntity.badRequest().body("Out of stock");
            }

            product.setReservedStock(reserved + buyingStock);
            inventoryRepository.save(product);
            return ResponseEntity.ok("Inventory updated successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while updating inventory.");
        }
    }

    @Transactional
    public ResponseEntity<?> reserveInventory(InventoryRequest req) {
        try {
            if (req == null) {
                return ResponseEntity.badRequest().body("Request body must not be null");
            }
            if (req.getProductId() == null || req.getProductId().isBlank()) {
                return ResponseEntity.badRequest().body("Product id must not be null or blank");
            }
            if (req.getStock() == null || req.getStock() <= 0) {
                return ResponseEntity.badRequest().body("Stock must be greater than 0");
            }

            boolean exists = inventoryRepository.findByProductId(req.getProductId()).isPresent();
            if (!exists) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found for id: " + req.getProductId());
            }

            int updatedRows = inventoryRepository.reserveStock(req.getProductId(), req.getStock());
            if (updatedRows > 0) {
                return ResponseEntity.ok("Stock reserved successfully");
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Insufficient stock for product: " + req.getProductId());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while reserving inventory.");
        }
    }

    public ResponseEntity<?> GetInventory(String productId) {
        try {
            if (productId == null || productId.isBlank()) {
                return ResponseEntity.badRequest().body("Product id must not be null or blank");
            }

            InventoryEntity inventory = inventoryRepository.findByProductId(productId)
                    .orElse(new InventoryEntity());
            return ResponseEntity.ok(inventory);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while fetching inventory");
        }
    }

    @Transactional
    public ResponseEntity<?> createStock(InventoryRequest req) {
        try {
            if (req == null) {
                return ResponseEntity.badRequest().body("Request body must not be null");
            }
            if (req.getProductId() == null || req.getProductId().isBlank()) {
                return ResponseEntity.badRequest().body("Product id must not be null or blank");
            }
            if (req.getStock() == null || req.getStock() < 0) {
                return ResponseEntity.badRequest().body("Stock must not be null or negative");
            }

            String proid = req.getProductId();
            Optional<InventoryEntity> existingOpt = inventoryRepository.findByProductId(proid);
            InventoryEntity entityToSave = existingOpt.orElseGet(InventoryEntity::new);

            if (existingOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid product id");
            }

            entityToSave.setProductId(proid);
            entityToSave.setTotalStock(req.getStock());
            inventoryRepository.save(entityToSave);

            orderMessageProducer.sendOrderUpdate(
                    new OrderMessage(entityToSave.getProductId(), entityToSave.getTotalStock())
            );

            return ResponseEntity.ok("Inventory created successfully");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while creating inventory");
        }
    }

    @Transactional
    public ResponseEntity<?> updateStock(InventoryRequest req) {
        try {
            if (req == null) {
                return ResponseEntity.badRequest().body("Request body must not be null");
            }
            if (req.getProductId() == null || req.getProductId().isBlank()) {
                return ResponseEntity.badRequest().body("Product id must not be null or blank");
            }
            if (req.getStock() == null || req.getStock() < 0) {
                return ResponseEntity.badRequest().body("Stock must not be null or negative");
            }

            String proId = req.getProductId();
            Long stock = req.getStock();

            Optional<InventoryEntity> existingOpt = inventoryRepository.findByProductId(proId);
            if (existingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Inventory not founds for product: " + proId);
            }

            InventoryEntity inv = existingOpt.get();
            Long currentStock = inv.getTotalStock();
            long oldStock = currentStock == null ? 0L : currentStock;
            inv.setTotalStock(oldStock + stock);
            inventoryRepository.save(inv);
            return ResponseEntity.ok("stock updated");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while updating stock");
        }
    }
}
