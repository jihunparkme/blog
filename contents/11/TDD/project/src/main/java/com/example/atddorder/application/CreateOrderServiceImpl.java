package com.example.atddorder.application;

import com.example.atddorder.domain.PendingOrder;
import com.example.atddorder.domain.PendingOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class CreateOrderServiceImpl implements CreateOrderService {

    private final PendingOrderRepository pendingOrderRepository;

    @Override
    public PendingOrder createPendingOrder(PendingOrderRequest request) {
        PendingOrder pendingOrder = PendingOrder.create(request.getProductId(), request.getQuantity());
        return pendingOrderRepository.save(pendingOrder);
    }
}
