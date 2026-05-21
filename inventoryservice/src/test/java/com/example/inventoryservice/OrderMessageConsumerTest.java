package com.example.inventoryservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderMessageConsumerTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderMessageProducer orderMessageProducer;

    @InjectMocks
    private OrderMessageConsumer orderMessageConsumer;

    @Test
    @DisplayName("OrderMessageConsumer reserves stock and sends confirmation on success")
    void consume_reservesStockAndSendsConfirmation() {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setId(1L);
        inventory.setProductId("product-1");
        inventory.setTotalStock(10L);
        inventory.setReservedStock(2L);

        when(inventoryRepository.findByProductId("product-1")).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(InventoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderMessage message = new OrderMessage();
        message.setOrderId("order-1");
        message.setProductId("product-1");
        message.setStock(3L);
        message.setStatus("RESERVED");

        orderMessageConsumer.consume(message);

        assertThat(inventory.getReservedStock()).isEqualTo(5L);
        assertThat(inventory.getTotalStock()).isEqualTo(7L);

        ArgumentCaptor<OrderMessage> confirmationCaptor = ArgumentCaptor.forClass(OrderMessage.class);
        verify(orderMessageProducer).sendOrderUpdate(confirmationCaptor.capture());
        
        OrderMessage confirmation = confirmationCaptor.getValue();
        assertThat(confirmation.getOrderId()).isEqualTo("order-1");
        assertThat(confirmation.getProductId()).isEqualTo("product-1");
        assertThat(confirmation.getStock()).isEqualTo(7L);
        assertThat(confirmation.getStatus()).isEqualTo("RESERVATION_CONFIRMED");

        verify(inventoryRepository).findByProductId("product-1");
        verify(inventoryRepository).save(inventory);
    }

    @Test
    @DisplayName("OrderMessageConsumer sends failure confirmation when stock insufficient")
    void consume_sendsFaiureConfirmationOnInsufficientStock() {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setId(1L);
        inventory.setProductId("product-1");
        inventory.setTotalStock(2L);
        inventory.setReservedStock(0L);

        when(inventoryRepository.findByProductId("product-1")).thenReturn(Optional.of(inventory));

        OrderMessage message = new OrderMessage();
        message.setOrderId("order-2");
        message.setProductId("product-1");
        message.setStock(5L);
        message.setStatus("RESERVED");

        orderMessageConsumer.consume(message);

        // Stock should not be modified
        assertThat(inventory.getReservedStock()).isEqualTo(0L);
        assertThat(inventory.getTotalStock()).isEqualTo(2L);

        ArgumentCaptor<OrderMessage> confirmationCaptor = ArgumentCaptor.forClass(OrderMessage.class);
        verify(orderMessageProducer).sendOrderUpdate(confirmationCaptor.capture());
        
        OrderMessage confirmation = confirmationCaptor.getValue();
        assertThat(confirmation.getOrderId()).isEqualTo("order-2");
        assertThat(confirmation.getProductId()).isEqualTo("product-1");
        assertThat(confirmation.getStatus()).isEqualTo("RESERVATION_FAILED");
    }

    @Test
    @DisplayName("OrderMessageConsumer releases reserved stock on cancellation")
    void consume_releasesStockOnCancellation() {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setId(1L);
        inventory.setProductId("product-1");
        inventory.setTotalStock(7L);
        inventory.setReservedStock(3L);

        when(inventoryRepository.findByProductId("product-1")).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(InventoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderMessage message = new OrderMessage();
        message.setOrderId("order-3");
        message.setProductId("product-1");
        message.setStock(-3L);  // Negative stock indicates release
        message.setStatus("CANCELLED");

        orderMessageConsumer.consume(message);

        // Stock should be restored
        assertThat(inventory.getReservedStock()).isEqualTo(0L);
        assertThat(inventory.getTotalStock()).isEqualTo(10L);

        ArgumentCaptor<OrderMessage> confirmationCaptor = ArgumentCaptor.forClass(OrderMessage.class);
        verify(orderMessageProducer).sendOrderUpdate(confirmationCaptor.capture());
        
        OrderMessage confirmation = confirmationCaptor.getValue();
        assertThat(confirmation.getStatus()).isEqualTo("RESERVATION_CONFIRMED");

        verify(inventoryRepository).save(inventory);
    }
}
