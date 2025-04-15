package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.model.DeliveryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryItemRepository extends JpaRepository<DeliveryItem, Long> {
}
