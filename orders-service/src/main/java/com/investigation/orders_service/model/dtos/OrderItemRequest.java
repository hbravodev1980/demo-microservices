package com.investigation.orders_service.model.dtos;

public record OrderItemRequest(Long id, String sku, Double price, Integer quantity) {

}