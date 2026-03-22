package com.example.inventoryservice;


import java.time.LocalDateTime;

import jakarta.persistence.Table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import lombok.*;

import jakarta.persistence.Transient;

@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventory")
public class InventoryEntity {
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name="productId")
    private String productId;
    @Column(name="productName")
    private String productName;
    @Column(name="totalStock")
    private Long totalStock;
    @Column(name="reservedStock")
    private Long reservedStock;

    @Transient
    private Long AvailableStock(){
        return totalStock - reservedStock;
    }
    @Version
    private Long version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate(){


        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if(reservedStock == null) reservedStock = 0L;

   


}
