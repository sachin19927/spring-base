package com.practice.core.mapper;

import com.practice.core.dto.DeliveryRequest;
import com.practice.core.dto.DeliveryResponse;
import com.practice.core.entity.Delivery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {
    DeliveryResponse toResponse(Delivery delivery);
}
