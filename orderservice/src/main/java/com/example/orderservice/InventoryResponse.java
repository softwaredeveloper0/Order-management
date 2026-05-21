package com.example.orderservice;

import java.io.Serializable;

public class InventoryResponse implements Serializable {
    private String productId;
    private Long totalStock;
    private Long reservedStock;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Long getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Long totalStock) {
        this.totalStock = totalStock;
    }

    public Long getReservedStock() {
        return reservedStock;
    }

    public void setReservedStock(Long reservedStock) {
        this.reservedStock = reservedStock;
    }
}
