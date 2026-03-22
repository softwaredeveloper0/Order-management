package com.example.productservice;

import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductRepository extends JpaRepository<ProductEntity, Long>{

    ProductEntity findByProductId(String productId);
    ProductEntity findByProductName(String productName);
    
}
