package com.example.atddorder.application;

import com.example.atddorder.domain.PendingOrder;
import com.example.atddorder.domain.PendingOrderRepository;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class CreateOrderServiceTest {
    private CreateOrderService createOrderService = new CreateOrderServiceImpl();

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

    /**
     * inner class 에 static 을 추가해서 GC가 안되는 문제를 해결
     */
    private static class CreateOrderServiceImpl implements CreateOrderService {
        private final PendingOrderRepository pendingOrderRepository = new PendingOrderRepositoryMemoryImpl();

        @Override
        public PendingOrder createPendingOrder(PendingOrderRequest request) {
             PendingOrder pendingOrder = PendingOrder.create(request.getProductId(), request.getQuantity());
            return pendingOrderRepository.save(pendingOrder);
        }
    }

    private static class PendingOrderRepositoryMemoryImpl implements PendingOrderRepository {
        private final AtomicLong atomicId = new AtomicLong(1);

        @Override
        public PendingOrder save(PendingOrder pendingOrder) {
            pendingOrder.assignId(nextId());
            return pendingOrder;
        }

        private long nextId() {
            return atomicId.getAndIncrement();
        }
    }
}