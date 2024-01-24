package com.investigation.orders_service.model.dtos;

import java.util.List;

public record OrderRequest(List<OrderItemRequest> orderItemRequests) {

}