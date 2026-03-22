package com.example.inventoryservice;

import lombok.Data;

@Data
public class InventoryRequest {

    private String productId;
    private Long stock;

    public String getProductId() { 
        return productId; 
    }
    public Long getStock() { 
        return stock; 
    }
    public void setProductId(String productId) { 
        this.productId = productId; 
    }
    public void setStock(Long stock) { 
        this.stock = stock; 
    }
}
