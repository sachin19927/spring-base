package com.practice.core.dto;

import com.practice.core.model.DeliveryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record DeliveryRequest(
        @NotBlank(message = "VehicleID must not be blank") String vehicleId,
        @NotBlank(message = "Address must not be blank") String address,
        @NotNull(message = "StartedAt is required") Instant startedAt,
        @NotNull(message = "Status is required")DeliveryStatus status

) {
}
