package com.practice.core.service;

import com.practice.core.dto.DeliveryRequest;
import com.practice.core.dto.DeliveryResponse;
import com.practice.core.entity.Delivery;
import com.practice.core.execption.BusinessValidationException;
import com.practice.core.execption.ResourceNotFoundException;
import com.practice.core.mapper.DeliveryMapper;
import com.practice.core.model.ErrorCode;
import com.practice.core.repository.DeliveryRepository;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DeliveryService {

    private final DeliveryMapper deliveryMapper;
    private final DeliveryRepository deliveryRepository;
    private final Clock clock;

    @Transactional
    public DeliveryResponse createDelivery(DeliveryRequest deliveryRequest) {
        validateDelivery(deliveryRequest);
        Delivery delivery = Delivery.create(
                deliveryRequest.vehicleId(),
                deliveryRequest.address(),
                deliveryRequest.startedAt(),
                deliveryRequest.status(),
                deliveryRequest.finishedAt()
        );
        Delivery savedDelivery = deliveryRepository.save(delivery);
        return deliveryMapper.toResponse(savedDelivery);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getDelivery(UUID id){
        Delivery delivery = deliveryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ErrorCode.DELIVERY_NOT_FOUND,"Delivery not found"));
        return deliveryMapper.toResponse(delivery);
    }

    private void validateDelivery(DeliveryRequest request){
        if(request.startedAt().isAfter(Instant.now(clock))) {
            throw new BusinessValidationException(ErrorCode.STARTED_AT_IN_FUTURE, "Started at cannot be in the future");
        }
    }

}
