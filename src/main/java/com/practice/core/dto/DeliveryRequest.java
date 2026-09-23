package com.practice.core.dto;

import com.practice.core.model.DeliveryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record DeliveryRequest(
        @NotBlank(message = "VehicleID must not be blank")
        @Size(max = 50, message = "VehicleID must not exceed 50 characters")
        String vehicleId,

        @NotBlank(message = "Address must not be blank")
        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address,

        @NotNull(message = "StartedAt is required") Instant startedAt,
        @NotNull(message = "Status is required") DeliveryStatus status,
        Instant finishedAt) {}
