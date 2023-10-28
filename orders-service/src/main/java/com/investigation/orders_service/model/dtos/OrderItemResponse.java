package com.investigation.orders_service.model.dtos;

public record OrderItemResponse(Long id, String sku, Double price, Integer quantity) {
}