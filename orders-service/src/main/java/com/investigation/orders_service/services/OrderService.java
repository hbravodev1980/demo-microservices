package com.investigation.orders_service.services;

import com.investigation.orders_service.model.dtos.BaseResponse;
import com.investigation.orders_service.model.dtos.OrderItemRequest;
import com.investigation.orders_service.model.dtos.OrderItemResponse;
import com.investigation.orders_service.model.dtos.OrderRequest;
import com.investigation.orders_service.model.dtos.OrderResponse;
import com.investigation.orders_service.model.entities.Order;
import com.investigation.orders_service.model.entities.OrderItem;
import com.investigation.orders_service.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest orderRequest) {
        BaseResponse result = webClientBuilder.build()
                .post()
                .uri("lb://inventories-service/api/inventories/in-stock")
                .bodyValue(orderRequest.getOrderItemRequests())
                .retrieve()
                .bodyToMono(BaseResponse.class)
                .block();

        if (result == null || result.hasErrors()) {
            throw new IllegalArgumentException("Some of the products are not in stock.");
        }
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderItems(orderRequest.getOrderItemRequests().stream()
                .map(orderItemRequest -> mapToOrderItem(orderItemRequest, order))
                .toList());
        orderRepository.save(order);
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::mapToOrderResponse).toList();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderItems().stream()
                        .map(this::mapToOrderItemResponse)
                        .toList());
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getSku(),
                orderItem.getPrice(),
                orderItem.getQuantity());
    }

    private OrderItem mapToOrderItem(OrderItemRequest orderItemRequest, Order order) {
        return OrderItem.builder()
                .id(orderItemRequest.getId())
                .sku(orderItemRequest.getSku())
                .price(orderItemRequest.getPrice())
                .quantity(orderItemRequest.getQuantity())
                .order(order)
                .build();
    }
}