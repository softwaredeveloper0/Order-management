package com.example.inventoryservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderMessageProducer orderMessageProducer;

    @Mock
    private ProductMessageConsumer productMessageConsumer;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("UpdateInventory rejects negative buying stock")
    void updateInventory_negativeStock_returnsBadRequest() {
        ResponseEntity<?> response = inventoryService.UpdateInventory("p1", -1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Updated stock cannot be less than 0");
        verifyNoMoreInteractions(inventoryRepository, orderMessageProducer, productMessageConsumer);
    }

    @Test
    @DisplayName("UpdateInventory returns 404 when product missing")
    void updateInventory_missingProduct_returnsNotFound() {
        when(inventoryRepository.findByProductId("missing")).thenReturn(Optional.empty());

        ResponseEntity<?> response = inventoryService.UpdateInventory("missing", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(inventoryRepository).findByProductId("missing");
        verifyNoMoreInteractions(inventoryRepository, orderMessageProducer, productMessageConsumer);
    }

    @Test
    @DisplayName("UpdateInventory reserves stock and persists when valid")
    void updateInventory_validRequest_updatesReservedStock() {
        InventoryEntity existing = new InventoryEntity();
        existing.setProductId("p1");
        existing.setTotalStock(5L);
        existing.setReservedStock(1L);

        when(inventoryRepository.findByProductId("p1")).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(InventoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = inventoryService.UpdateInventory("p1", 2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Inventory updated successfully");
        assertThat(existing.getReservedStock()).isEqualTo(3L);

        verify(inventoryRepository).findByProductId("p1");
        verify(inventoryRepository).save(existing);
        verifyNoMoreInteractions(inventoryRepository, orderMessageProducer, productMessageConsumer);
    }
}

