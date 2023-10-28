package com.investigation.inventory_service.services;

import com.investigation.inventory_service.model.dtos.BaseResponse;
import com.investigation.inventory_service.model.dtos.OrderItemRequest;
import com.investigation.inventory_service.model.entities.Inventory;
import com.investigation.inventory_service.repositories.InventoryRepository;
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
        List<String> skus = orderItemRequests.stream().map(OrderItemRequest::getSku).toList();
        List<Inventory> inventories = inventoryRepository.findBySkuIn(skus);

        orderItemRequests.forEach(orderItemRequest -> {
                Optional<Inventory> inventoryOptional = inventories.stream()
                        .filter(inventory -> inventory.getSku().equals(orderItemRequest.getSku()))
                        .findFirst();

                if (inventoryOptional.isEmpty()) {
                    errors.add("Product with sku " + orderItemRequest.getSku() + " does not exist");
                } else if (inventoryOptional.get().getQuantity() < orderItemRequest.getQuantity()) {
                    errors.add("Product with sku " + orderItemRequest.getSku() + " has insufficient quantity");
                }
        });
        return new BaseResponse(errors);
    }
}