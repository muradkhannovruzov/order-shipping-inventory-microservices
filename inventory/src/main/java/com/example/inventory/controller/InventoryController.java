package com.example.inventory.controller;

import com.example.inventory.dto.InventoryStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InventoryController {

    private Map<String, InventoryStatus> statuses =
            Map.of("1", new InventoryStatus(true),
                    "2", new InventoryStatus(false));

    @GetMapping("/inventories")
    public InventoryStatus getInventory(@RequestParam("productId") String productId){
        return statuses.getOrDefault(productId, new InventoryStatus(false));
    }
}
