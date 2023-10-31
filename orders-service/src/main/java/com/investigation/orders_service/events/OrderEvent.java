package com.investigation.orders_service.events;

import com.investigation.orders_service.model.enums.OrderStatus;

public record OrderEvent(String orderNumber, int itemsCount, OrderStatus orderStatus) { }