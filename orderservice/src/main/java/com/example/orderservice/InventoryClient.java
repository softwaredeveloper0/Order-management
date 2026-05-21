package com.example.orderservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "INVENTORYSERVICE")
public interface InventoryClient {

    @GetMapping("/inventory/{productId}")
    ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable String productId);

    @PostMapping("/inventory/reserve")
    ResponseEntity<String> reserveInventory(@RequestBody InventoryRequest request);
}
