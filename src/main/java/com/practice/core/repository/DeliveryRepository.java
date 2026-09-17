package com.practice.core.repository;

import com.practice.core.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
}
