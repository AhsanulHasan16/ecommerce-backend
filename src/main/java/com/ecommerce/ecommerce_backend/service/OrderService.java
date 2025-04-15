package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.OrderRequest;
import com.ecommerce.ecommerce_backend.model.Order;
import com.ecommerce.ecommerce_backend.model.OrderHistory;

import java.util.List;

public interface OrderService {

    List<Order> getAllOrders();

    Order placeOrder(OrderRequest orderRequest);

    Order editOrder(Long orderId, OrderRequest orderRequest);

    void deleteOrder(Long orderId);

    Order getOrderById(Long orderId);

    Order updateOrderStatus(Long orderId, String status);

    List<OrderHistory> getOrderHistory(Long orderId);

}
