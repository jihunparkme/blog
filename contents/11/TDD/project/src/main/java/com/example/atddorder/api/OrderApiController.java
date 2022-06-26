package com.example.atddorder.api;

import com.example.atddorder.application.PendingOrderRequest;
import com.example.atddorder.application.PendingOrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderApiController {
    @PostMapping("/orders/pendingOrder")
    public ResponseEntity<PendingOrderResponse> createPendingOrder(@RequestBody PendingOrderRequest request) {
        PendingOrderResponse response = new PendingOrderResponse(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(response);
    }
}
