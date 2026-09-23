package com.practice.core.dto;

import com.practice.core.model.DeliveryStatus;
import java.time.Instant;
import java.util.UUID;

public record DeliveryResponse(
        UUID id, String vehicleId, String address, Instant startedAt, DeliveryStatus status, Instant finishedAt) {}
