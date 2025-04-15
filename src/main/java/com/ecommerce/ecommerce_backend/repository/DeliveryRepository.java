package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
