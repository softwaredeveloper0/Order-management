package com.example.productservice;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    
    private final ProductClient productClient;

    private final ProductMessageProducer productMessageProducer;
    
    public ProductService(ProductRepository productRepository,ProductClient productClient,ProductMessageProducer productMessageProducer){
        this.productRepository = productRepository;
        this.productClient = productClient;
        this.productMessageProducer = productMessageProducer;
    }

    public ResponseEntity<?> CreateProduct(@RequestBody ProductEntity product){

        productRepository.save(product);
        List<ProductEntity> products = productRepository.findAll();

        
        productMessageProducer.sendProductUpdate(
            new ProductMessage(product.getProductId(), product.getProductName())
    );
    

        return ResponseEntity.ok("Product created successfully.");
    }

    public ProductDTO User(@RequestParam String name){

        ProductEntity id = productRepository.findByProductName(name);
        ProductDTO productDTO = productClient.getUser(id.getId());

        return productDTO;
        
        
    }
}
