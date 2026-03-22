package com.example.productservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "USERSERVICE")
public interface ProductClient {
    
    @GetMapping("/users/{id}")
    ProductDTO getUser(@PathVariable long id);

    
}
