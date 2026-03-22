package com.example.orderservice;

import lombok.Data;

@Data
public class ProductResponse {
    private Long id;
    private String productId;
    private String productName;
    private String description;
    private Double price;

    public ProductResponse(Long id, String productId, String productName, String description, Double price) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.price = price;
    }
    public ProductResponse() {
        // required for deserialization
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }
}

