package com.example.atddorder.application;

import com.example.atddorder.domain.PendingOrder;
import lombok.Getter;

@Getter
public class PendingOrderResponse {
    private Long id;
    private Long productId;
    private int quantity;

    public PendingOrderResponse(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public PendingOrderResponse(PendingOrder pendingOrder) {
        this.id = pendingOrder.getId();
        this.productId = pendingOrder.getProductId();
        this.quantity = pendingOrder.getQuantity();
    }
}
