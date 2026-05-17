package com.example.inventoryservice;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductMessageConsumer consumer;
    private final OrderMessageProducer orderMessageProducer;

    public InventoryService(InventoryRepository inventoryRepository,
                            OrderMessageProducer orderMessageProducer,
                            ProductMessageConsumer consumer) {
        this.inventoryRepository = inventoryRepository;
        this.orderMessageProducer = orderMessageProducer;
        this.consumer = consumer;
    }

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

            long total = product.getTotalStock() == null ? 0L : product.getTotalStock();
            long reserved = product.getReservedStock() == null ? 0L : product.getReservedStock();
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
                    .body("An unexpected error occurred while updating inventory");
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
            long oldStock = inv.getTotalStock() == null ? 0L : inv.getTotalStock();
            inv.setTotalStock(oldStock + stock);
            inventoryRepository.save(inv);
            return ResponseEntity.ok("stock updated");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while updating stock");
        }
    }
}
