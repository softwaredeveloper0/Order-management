package com.example.inventoryservice;




import java.io.Serializable;

public class ProductMessage implements Serializable {
    private String productId;
    private String productName;

    public ProductMessage(String productId, String productName) {
        this.productId = productId;
        this.productName = productName;
    }

    public ProductMessage() {
        // required for deserialization
    }
    // getters
   
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

    // setters + constructors if needed
}


