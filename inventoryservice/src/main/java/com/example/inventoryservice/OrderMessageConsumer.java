package com.example.inventoryservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class OrderMessageConsumer {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderMessageProducer orderMessageProducer;
    

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    @Transactional
    public void consume(OrderMessage message) {

        System.out.println("Received order message: " + message);

        if (message == null || message.getProductId() == null) {
            System.out.println("Invalid order message received: productId is missing");
            return;
        }

        InventoryEntity inventory = inventoryRepository.findByProductId(message.getProductId()).orElse(null);
        if (inventory == null) {
            System.out.println("Inventory not found for product: " + message.getProductId() + ". Cannot reserve stock.");
            return;
        }

        Long stock = message.getStock() == null ? 0L : message.getStock();
        Long reservedStock = inventory.getReservedStock() == null ? 0L : inventory.getReservedStock();
        Long totalStock = inventory.getTotalStock() == null ? 0L : inventory.getTotalStock();

        boolean reservationSuccess = false;

        if ("CANCELLED".equalsIgnoreCase(message.getStatus()) || stock < 0) {
            Long quantityToRelease = Math.abs(stock);
            int updatedRows = inventoryRepository.releaseStock(message.getProductId(), quantityToRelease);
            if (updatedRows > 0) {
                System.out.println("Released " + quantityToRelease + " units of reserved stock for product: " + message.getProductId());
                reservationSuccess = true;
            } else {
                System.out.println("Warning: Cannot release " + quantityToRelease + " units. Only " + reservedStock + " units are reserved.");
            }
        } else {
            int updatedRows = inventoryRepository.reserveStock(message.getProductId(), stock);
            if (updatedRows > 0) {
                System.out.println("Reserved " + stock + " units of stock for product: " + message.getProductId());
                reservationSuccess = true;
            } else {
                System.out.println("Not enough stock to reserve for product: " + message.getProductId() + ". Requested=" + stock + ", available=" + totalStock);
            }
        }

        InventoryEntity updatedInventory = inventoryRepository.findByProductId(message.getProductId()).orElse(inventory);
        Long availableStock = updatedInventory.getTotalStock() == null ? 0L : updatedInventory.getTotalStock();
        OrderMessage confirmationMessage = new OrderMessage();
        confirmationMessage.setOrderId(message.getOrderId());
        confirmationMessage.setProductId(message.getProductId());
        confirmationMessage.setStock(availableStock);
        confirmationMessage.setStatus(reservationSuccess ? "RESERVATION_CONFIRMED" : "RESERVATION_FAILED");
        
        orderMessageProducer.sendOrderUpdate(confirmationMessage);
        System.out.println("Sent reservation confirmation: " + confirmationMessage);
    }
    
}

