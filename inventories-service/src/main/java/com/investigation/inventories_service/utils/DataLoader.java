package com.investigation.inventories_service.utils;

import com.investigation.inventories_service.model.entities.Inventory;
import com.investigation.inventories_service.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final InventoryRepository inventoryRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading data...");

        if (inventoryRepository.findAll().isEmpty()) {
            inventoryRepository.saveAll(
                    List.of(
                            Inventory.builder().sku("000001").quantity(10).build(),
                            Inventory.builder().sku("000002").quantity(20).build(),
                            Inventory.builder().sku("000003").quantity(30).build(),
                            Inventory.builder().sku("000004").quantity(50).build(),
                            Inventory.builder().sku("000005").quantity(20).build()
                    )
            );
        }
        log.info("Data loaded.");
    }
}