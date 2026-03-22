package com.example.inventoryservice;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.inventoryservice.ProductMessage;


@Service
@RequiredArgsConstructor
public class ProductMessageConsumer {

    @Autowired
    private InventoryRepository inventoryRepository;
    

    @RabbitListener(queues = "productQueue")
    public void consume(ProductMessage message) {

        System.out.println("Received message: " + message);

        InventoryEntity inventory =
                inventoryRepository.findByProductId(message.getProductId())
                        .orElse(new InventoryEntity());

        inventory.setProductId(message.getProductId());
        inventory.setProductName(message.getProductName());

        inventoryRepository.save(inventory);

        System.out.println("Inventory updated successfully");
    }
    
}

