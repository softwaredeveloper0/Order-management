package com.example.productservice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductClient productClient;
    @Autowired
    private ProductService productService;
    


    @PostMapping("/products/create")
    public ResponseEntity<?> CreateProduct(@RequestBody ProductEntity product){

        return productService.CreateProduct(product);
    }
    
    @GetMapping("/product")
    public ProductDTO User(@RequestParam String name){

        return productService.User(name);
        
        
    }
    
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductEntity> getProductByProductId(@PathVariable String productId){
        ProductEntity product = productRepository.findByProductId(productId);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }
}
