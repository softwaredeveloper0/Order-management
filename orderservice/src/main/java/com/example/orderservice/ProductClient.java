package com.example.orderservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PRODUCTSERVICE")
public interface ProductClient {
    
    @GetMapping("/products/{productId}")
    ResponseEntity<ProductResponse> getProductByProductId(@PathVariable("productId") String productId);
}
