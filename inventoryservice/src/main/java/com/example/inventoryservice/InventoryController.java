package com.example.inventoryservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.transaction.Transactional;

@RestController
public class InventoryController {
    
    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/inventory/create")
    public ResponseEntity<?> createInventory(@RequestBody InventoryRequest req){


        
        return ResponseEntity.ok(inventoryService.createStock(req));
    }

    @GetMapping("/inventory/{productId}")
    public ResponseEntity<?> getInventory(@PathVariable String productId){
        
        return inventoryService.GetInventory(productId);
    }

    @Transactional
    @PostMapping("/inventory/update")
    public ResponseEntity<?> updateInventory(@RequestBody String productId,Long buyingStock){



        return inventoryService.UpdateInventory(productId, buyingStock);
    }
        @PostMapping("/inventory/updatestock")
    public ResponseEntity<?> updateStock(@RequestBody InventoryRequest req){



        return ResponseEntity.ok(inventoryService.updateStock(req));
    }



    
    




}
