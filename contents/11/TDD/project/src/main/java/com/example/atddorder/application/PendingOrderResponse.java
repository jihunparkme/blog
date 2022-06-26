package com.example.atddorder.application;

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
}
