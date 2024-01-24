package com.investigation.inventories_service.services;

import com.investigation.inventories_service.model.dtos.BaseResponse;
import com.investigation.inventories_service.model.dtos.OrderItemRequest;
import com.investigation.inventories_service.model.entities.Inventory;
import com.investigation.inventories_service.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public boolean isInStock(String sku) {
        Optional<Inventory> inventoryOptional = inventoryRepository.findBySku(sku);
        return inventoryOptional.filter(inventory -> inventory.getQuantity() > 0).isPresent();
    }

    public BaseResponse areInStock(List<OrderItemRequest> orderItemRequests) {
        List<String> errors = new ArrayList<>();
        List<String> skus = orderItemRequests.stream().map(OrderItemRequest::sku).toList();
        List<Inventory> inventories = inventoryRepository.findBySkuIn(skus);

        orderItemRequests.forEach(orderItemRequest -> {
                Optional<Inventory> inventoryOptional = inventories.stream()
                        .filter(inventory -> inventory.getSku().equals(orderItemRequest.sku()))
                        .findFirst();

                if (inventoryOptional.isEmpty()) {
                    errors.add("Product with sku " + orderItemRequest.sku() + " does not exist");
                } else if (inventoryOptional.get().getQuantity() < orderItemRequest.quantity()) {
                    errors.add("Product with sku " + orderItemRequest.sku() + " has insufficient quantity");
                }
        });
        return new BaseResponse(errors);
    }
}