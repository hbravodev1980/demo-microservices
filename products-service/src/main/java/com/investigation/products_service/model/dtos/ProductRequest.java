package com.investigation.products_service.model.dtos;

public record ProductRequest(String sku, String name, String description, Double price, Boolean status) {

}