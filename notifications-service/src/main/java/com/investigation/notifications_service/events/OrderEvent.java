package com.investigation.notifications_service.events;

import com.investigation.notifications_service.model.enums.OrderStatus;

public record OrderEvent(String orderNumber, int itemsCount, OrderStatus orderStatus) {

}