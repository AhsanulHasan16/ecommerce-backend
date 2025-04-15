package com.ecommerce.ecommerce_backend.schedule;

import com.ecommerce.ecommerce_backend.model.Delivery;
import com.ecommerce.ecommerce_backend.model.DeliveryItem;
import com.ecommerce.ecommerce_backend.model.Order;
import com.ecommerce.ecommerce_backend.repository.DeliveryItemRepository;
import com.ecommerce.ecommerce_backend.repository.DeliveryRepository;
import com.ecommerce.ecommerce_backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScheduledDeliveryTask {

    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryItemRepository deliveryItemRepository;

    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void moveDeliveredOrders() {
        List<Order> deliveredOrders = orderRepository.findByStatus("DELIVERED");

        deliveredOrders.forEach(order -> {
            // Creating Delivery from Order
            Delivery delivery = Delivery.builder()
                    .user(order.getUser())
                    .status(order.getStatus())
                    .createdAt(LocalDateTime.now()) // Use delivery timestamp
                    .build();

            Delivery savedDelivery = deliveryRepository.save(delivery);

            // Creating DeliveryItems from OrderItems
            List<DeliveryItem> deliveryItems = order.getItems().stream()
                    .map(orderItem -> DeliveryItem.builder()
                            .delivery(savedDelivery)
                            .product(orderItem.getProduct())
                            .quantity(orderItem.getQuantity())
                            .priceAtDelivery(orderItem.getPriceAtOrder())
                            .build())
                    .collect(Collectors.toList());

            deliveryItemRepository.saveAll(deliveryItems);

            // Deleting the original Order
            orderRepository.delete(order);
        });

    }

}
