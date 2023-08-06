package com.example.order.service;

import com.example.order.dto.PlaceOrderRequest;
import com.example.order.entity.Order;
import com.example.order.enums.OrderStatus;
import com.example.order.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaTemplate kafkaTemplate;
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public void placeOrder(PlaceOrderRequest request) {
        InventoryStatus status = inventoryClient.exists(request.getProduct());

        if(!status.isExists()){
            throw new EntityNotFoundException("Product does not exists");
        }
        Order order = new Order();
        order.setProduct(request.getProduct());
        order.setPrice(request.getPrice());
        order.setStatus(OrderStatus.PLACED);

        Order o = orderRepository.save(order);

        kafkaTemplate.send("prod.order.places", String.valueOf(o.getId()), OrderPlaceEvent.builder()
                .orderId(order.getId())
                .product(request.getProduct())
                .price(request.getPrice())
                .build());
    }

    @KafkaListener(topics = "prod.order.shipped", groupId = "order-group")
    public void handleOrderShippedEvent(String orderId){
        orderRepository.findById(Long.valueOf(orderId)).ifPresent(order -> {
            order.setStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);
        });
    }

}

@Data
@Builder
class OrderPlaceEvent{
    private Long orderId;
    private String product;
    private double price;
}


@FeignClient(url = "http://localhost:8082", name = "inventories")
interface InventoryClient {
    @GetMapping("/inventories")
    InventoryStatus exists(@RequestParam("productId") String productId);
}

@Data
class InventoryStatus {
    private boolean exists;
}