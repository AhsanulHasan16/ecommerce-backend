package com.ecommerce.ecommerce_backend.service.impl;

import com.ecommerce.ecommerce_backend.dto.OrderRequest;
import com.ecommerce.ecommerce_backend.model.*;
import com.ecommerce.ecommerce_backend.repository.OrderHistoryRepository;
import com.ecommerce.ecommerce_backend.repository.OrderRepository;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import com.ecommerce.ecommerce_backend.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<Order> getAllOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found"));
        return orderRepository.findByUserId(user.getId());
    }

    @Transactional
    public Order placeOrder(OrderRequest orderRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found"));

        List<OrderItem> items = new ArrayList<>();

        for (OrderRequest.Item itemRequest : orderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product Not Found"));

            if (product.getQty() < itemRequest.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sorry, Insufficient Stock");
            }

//            product.setQty(product.getQty() - itemRequest.getQuantity());
//            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .priceAtOrder(product.getPrice())
                    .build();
            items.add(orderItem);
        }

        Order order = Order.builder()
                .user(user)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .items(items)
                .build();

        items.forEach(item -> item.setOrder(order));
        Order savedOrder = orderRepository.save(order);

        OrderHistory history = OrderHistory.builder()
                .order(savedOrder)
                .action("CREATED")
                .timestamp(LocalDateTime.now())
                .details("Order placed with " + items.size() + " items")
                .build();

        orderHistoryRepository.save(history);

//        TODO: Send notification to admin.

        return savedOrder;

    }

    @Transactional
    public Order editOrder(Long orderId, OrderRequest orderRequest) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order Not Found"));

        // Edit an order until approved or rejected by the admin
        if (!order.getStatus().equals("PENDING")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order Cannot Be Edited Now");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found"));

        if (!user.getId().equals(order.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This Order Does Not Belong To You");
        }

        // Updating product stock. Adding back the old quantity.
//        for (OrderItem oldItem : order.getItems()) {
//            Product product = oldItem.getProduct();
//            product.setQty(product.getQty() + oldItem.getQuantity());
//            productRepository.save(product);
//        }

        // Updating product stock. Subtracting the new quantity.
        List<OrderItem> newItems = new ArrayList<>();

        for (OrderRequest.Item itemRequest : orderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product Not Found"));

            if (product.getQty() < itemRequest.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sorry, Insufficient Stock");
            }

//            product.setQty(product.getQty() - itemRequest.getQuantity());
//            productRepository.save(product);

            // Adding new item to the same existing order
            OrderItem newItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .priceAtOrder(product.getPrice())
                    .order(order)
                    .build();
            newItems.add(newItem);
        }

        // Updating order. Removing old items. Adding new items. And preserving the original order date.
        order.getItems().clear();
        order.getItems().addAll(newItems);
        order.setCreatedAt(order.getCreatedAt());


        // Updating order history.
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .action("EDITED")
                .timestamp(LocalDateTime.now())
                .details("Order modified")
                .build();

        orderHistoryRepository.save(history);

        return orderRepository.save(order);
    }

    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order Not Found"));
        orderRepository.delete(order);
    }


    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order Not Found"));
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order Not Found"));
        if (!status.equals("APPROVED") && !status.equals("REJECTED") && !status.equals("DELIVERED")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Status");
        }

        // When the admin marks an order as delivered, the qty of the product should be updated.
        if (status.equals("DELIVERED")) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                int orderedQty = item.getQuantity();

                if (product.getQty() < orderedQty) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Insufficient Stock For Product: " + product.getName()
                    );
                }

                product.setQty(product.getQty() - orderedQty);
                productRepository.save(product);
            }
        }


        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);

        OrderHistory history = OrderHistory.builder()
                .order(savedOrder)
                .action(status)
                .timestamp(LocalDateTime.now())
                .details("Order marked as " + status)
                .build();

        orderHistoryRepository.save(history);

        return savedOrder;

    }

    public List<OrderHistory> getOrderHistory(Long orderId) {
        return orderHistoryRepository.findByOrderId(orderId);
    }

}
