package com.ecommerce.ecommerce_backend.event;


import com.ecommerce.ecommerce_backend.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderNotificationListener {

    @EventListener
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        Order order = event.getOrder();
        log.info("📢 Admin Notification: New Order #{} Placed By {} (Total Items: {})",
                order.getId(),
                order.getUser().getName(),
                order.getItems().size()
        );
    }

}
