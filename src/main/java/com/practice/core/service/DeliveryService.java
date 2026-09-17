package com.practice.core.service;

import com.practice.core.dto.DeliveryRequest;
import com.practice.core.dto.DeliveryResponse;
import com.practice.core.entity.Delivery;
import com.practice.core.mapper.DeliveryMapper;
import com.practice.core.repository.DeliveryRepository;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class DeliveryService {

    private final DeliveryMapper deliveryMapper;
    private final DeliveryRepository deliveryRepository;

    public DeliveryResponse createDelivery(DeliveryRequest deliveryRequest) {
        // Implementation for creating a delivery
        Delivery delivery = deliveryMapper.toEntity(deliveryRequest);
        deliveryRepository.save(delivery);
        return deliveryMapper.toResponse(delivery);
    }

    public DeliveryResponse getDelivery(UUID id){
        Delivery delivery = deliveryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Delivery not found"));
        return deliveryMapper.toResponse(delivery);
    }


}
