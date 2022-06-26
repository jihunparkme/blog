package com.example.atddorder.api;

import com.example.atddorder.application.CreateOrderService;
import com.example.atddorder.application.PendingOrderRequest;
import com.example.atddorder.application.PendingOrderResponse;
import com.example.atddorder.domain.PendingOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final CreateOrderService createOrderService;

    @PostMapping("/orders/pendingOrder")
    public ResponseEntity<PendingOrderResponse> createPendingOrder(@RequestBody PendingOrderRequest request) {
        PendingOrder pendingOrder = createOrderService.createPendingOrder(request);
        PendingOrderResponse response = new PendingOrderResponse(pendingOrder);
        return ResponseEntity.ok(response);
    }
}
