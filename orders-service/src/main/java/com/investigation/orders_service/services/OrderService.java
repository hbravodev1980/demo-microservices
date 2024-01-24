package com.investigation.orders_service.services;

import com.investigation.orders_service.events.OrderEvent;
import com.investigation.orders_service.model.dtos.BaseResponse;
import com.investigation.orders_service.model.dtos.OrderItemRequest;
import com.investigation.orders_service.model.dtos.OrderItemResponse;
import com.investigation.orders_service.model.dtos.OrderRequest;
import com.investigation.orders_service.model.dtos.OrderResponse;
import com.investigation.orders_service.model.entities.Order;
import com.investigation.orders_service.model.entities.OrderItem;
import com.investigation.orders_service.model.enums.OrderStatus;
import com.investigation.orders_service.repositories.OrderRepository;
import com.investigation.orders_service.utils.JsonUtils;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObservationRegistry observationRegistry;

    private final static String ORDERS_TOPIC = "orders-topic";

    public OrderResponse placeOrder(OrderRequest orderRequest) {
        Observation inventoriesObservation = Observation.createNotStarted("inventories-service", observationRegistry);

        return inventoriesObservation.observe(() -> {
            BaseResponse result = webClientBuilder.build()
                    .post()
                    .uri("lb://inventories-service/api/inventories/in-stock")
                    .bodyValue(orderRequest.orderItemRequests())
                    .retrieve()
                    .bodyToMono(BaseResponse.class)
                    .block();

            if (result == null || result.hasErrors()) {
                throw new IllegalArgumentException("Some of the products are not in stock.");
            }
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setOrderItems(orderRequest.orderItemRequests().stream()
                    .map(orderItemRequest -> mapToOrderItem(orderItemRequest, order))
                    .toList());

            Order savedOrder = orderRepository.save(order);
            log.info("Order number {} with {} item(s) saved", savedOrder.getOrderNumber(), savedOrder.getOrderItems().size());

            // TODO: Send message to order topic
            log.info("Sending order number {} to queque {}", savedOrder.getOrderNumber(), ORDERS_TOPIC);

            kafkaTemplate.send(ORDERS_TOPIC,
                    JsonUtils.toJson(new OrderEvent(savedOrder.getOrderNumber(),
                            savedOrder.getOrderItems().size(), OrderStatus.PLACED)));

            return mapToOrderResponse(savedOrder);
        });
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
                .id(orderItemRequest.id())
                .sku(orderItemRequest.sku())
                .price(orderItemRequest.price())
                .quantity(orderItemRequest.quantity())
                .order(order)
                .build();
    }
}