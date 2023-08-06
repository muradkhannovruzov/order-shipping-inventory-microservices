package com.example.shipping.service;
import com.example.shipping.entity.Shipping;
import com.example.shipping.repository.ShippingRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShippingRepository shippingRepository;
    private final KafkaTemplate kafkaTemplate;

    @KafkaListener(topics = "prod.order.places", groupId = "shipping-group")
    public void handleOrderPlacedEvent(OrderPlacedEvent event){
        Shipping shipping = new Shipping();
        shipping.setOrderId(event.getOrderId());shippingRepository.save(shipping);

        kafkaTemplate.send("prod.order.shipped", String.valueOf(shipping.getOrderId()), String.valueOf(shipping.getOrderId()));
    }
}
@Data
@NoArgsConstructor
@AllArgsConstructor
class OrderPlacedEvent {

    private Long orderId;
    private String product;
    private double price;
}
