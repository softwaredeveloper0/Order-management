package com.example.inventoryservice;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.inventoryservice.ProductMessage;


@Service
@RequiredArgsConstructor
public class OrderMessageConsumer {

    @Autowired
    private InventoryRepository inventoryRepository;
    

    @RabbitListener(queues = "orderQueue")
    public void consume(OrderMessage message) {

        System.out.println("Received message: " + message);

        InventoryEntity inventory =
                inventoryRepository.findByProductId(message.getProductId())
                        .orElse(new InventoryEntity());
                // UPDATED

        // Handle different order statuses
        if ("CANCELLED".equals(message.getStatus()) || message.getStock() < 0) {
            // Release reserved stock back to total stock
            Long quantityToRelease = Math.abs(message.getStock());
            
            if (inventory.getReservedStock() >= quantityToRelease) {
                inventory.setReservedStock(inventory.getReservedStock() - quantityToRelease);
                // Optionally add back to total stock if needed
                // inventory.setTotalStock(inventory.getTotalStock() + quantityToRelease);
                
                inventoryRepository.save(inventory);
                System.out.println("Released " + quantityToRelease + " units of reserved stock for product: " + message.getProductId());
            } else {
                System.out.println("Warning: Cannot release " + quantityToRelease + 
                    " units. Only " + inventory.getReservedStock() + " units are reserved.");
            }
        } else {
            // Reserve stock (normal flow)
            inventory.setReservedStock(inventory.getReservedStock() + message.getStock());
            inventory.setTotalStock(inventory.getTotalStock() - message.getStock());
            inventoryRepository.save(inventory);
            System.out.println("Reserved " + message.getStock() + " units of stock for product: " + message.getProductId());
        }

        System.out.println("Inventory updated successfully");
    }
    
}
