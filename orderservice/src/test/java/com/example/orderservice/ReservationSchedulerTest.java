package com.example.orderservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationSchedulerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMessageProducer orderMessageProducer;

    @InjectMocks
    private ReservationScheduler reservationScheduler;

    private OrderEntity expiredOrder;

    @BeforeEach
    void setUp() {
        expiredOrder = new OrderEntity();
        expiredOrder.setOrderId("order-123");
        expiredOrder.setProductId("prod-456");
        expiredOrder.setQuantity(2L);
        expiredOrder.setStatus("RESERVED");
        expiredOrder.setReservedAt(LocalDateTime.now().minusMinutes(10));
    }

    @Test
    @DisplayName("Check expired reservations should do nothing when no expired orders")
    void checkExpiredReservations_shouldDoNothing_whenNoExpiredOrders() {
        when(orderRepository.findByStatusAndReservedAtBefore(anyString(), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        reservationScheduler.checkExpiredReservations();

        verify(orderRepository).findByStatusAndReservedAtBefore(eq("RESERVED"), any(LocalDateTime.class));
        verify(orderRepository, never()).save(any(OrderEntity.class));
        verify(orderMessageProducer, never()).sendOrderUpdate(any(OrderMessage.class));
    }

    @Test
    void checkExpiredReservations_shouldCancelExpiredOrders_whenExpiredOrdersExist() {
        when(orderRepository.findByStatusAndReservedAtBefore(anyString(), any(LocalDateTime.class)))
                .thenReturn(List.of(expiredOrder));

        reservationScheduler.checkExpiredReservations();

        verify(orderRepository).findByStatusAndReservedAtBefore(eq("RESERVED"), any(LocalDateTime.class));

        ArgumentCaptor<OrderEntity> orderEntityCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(orderEntityCaptor.capture());
        assertEquals("CANCELLED", orderEntityCaptor.getValue().getStatus());

        ArgumentCaptor<OrderMessage> orderMessageCaptor = ArgumentCaptor.forClass(OrderMessage.class);
        verify(orderMessageProducer).sendOrderUpdate(orderMessageCaptor.capture());
        assertEquals("order-123", orderMessageCaptor.getValue().getOrderId());
        assertEquals(-2, orderMessageCaptor.getValue().getStock());
        assertEquals("CANCELLED", orderMessageCaptor.getValue().getStatus());
    }
}