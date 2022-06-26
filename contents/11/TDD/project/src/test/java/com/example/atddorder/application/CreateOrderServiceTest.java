package com.example.atddorder.application;

import com.example.atddorder.domain.PendingOrder;
import com.example.atddorder.domain.PendingOrderRepository;
import com.example.atddorder.infra.PendingOrderRepositoryMemoryImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateOrderServiceTest {

    private PendingOrderRepository pendingOrderRepository = new PendingOrderRepositoryMemoryImpl();
    private CreateOrderService createOrderService = new CreateOrderServiceImpl(pendingOrderRepository);

    @Test
    void createPendingOrder() {
        // Given
        long productId = 1L;
        int quantity = 2;
        PendingOrderRequest request = new PendingOrderRequest(productId, quantity);

        // When
        PendingOrder pendingOrder = createOrderService.createPendingOrder(request);

        // Then (Step 01.)
        assertThat(pendingOrder.getId()).isPositive();
    }

}