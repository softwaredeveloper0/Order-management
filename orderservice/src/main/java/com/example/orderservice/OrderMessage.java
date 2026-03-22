package com.example.orderservice;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import java.io.Serializable;


@Data
public class OrderMessage implements Serializable{
        
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
    private String orderId;
    private String productId;
    private Double price;
    private Long stock;
    private String status;
    private Long reservedStock;


    public OrderMessage(String orderId, Long stock){

        this.orderId = orderId;
        this.stock = stock;
    }
    public OrderMessage() {
        // required for deserialization
    }

    


    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) {
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
        return reservedStock;
    }
    public void setReservedStock(Long reservedStock) {
        this.reservedStock = reservedStock;
    }
   
    


}
