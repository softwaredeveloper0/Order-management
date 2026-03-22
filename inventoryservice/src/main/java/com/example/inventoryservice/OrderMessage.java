package com.example.inventoryservice;

import java.io.Serializable;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;


@Data
public class OrderMessage implements Serializable{
        
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
    private Long orderId;
    private String productId;
    private Double price;
    private Long stock;
    private String status;
    private Long ReservedStock;

    public OrderMessage(String productId, Long stock){

        this.productId = productId;
        this.stock = stock;
    }

    public OrderMessage(Long orderId, Integer quantity){

        this.orderId = orderId;
        this.stock = quantity != null ? quantity.longValue() : null;
    }

    
    public OrderMessage() {
        // required for deserialization
    }


    public Long getOrderId() {
        return orderId;
    }
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }
    public Long getStock() {
        return stock;
    }
    public void setStock(Long stock) {
        this.stock = stock;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Long getReservedStock() {
        return ReservedStock;
    }
    public void setReservedStock(Long reservedStock) {
        ReservedStock = reservedStock;
    }

   
    


}


