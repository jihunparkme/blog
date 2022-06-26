package com.example.atddorder.domain;

import lombok.Getter;

@Getter
public class PendingOrder {
    private Long id;
    private long productId;
    private int quantity;

    public PendingOrder(long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public static PendingOrder create(Long productId, int quantity) {
        return new PendingOrder(productId, quantity);
    }

    public void assignId(long nextId) {
        this.id = nextId;
    }
}
