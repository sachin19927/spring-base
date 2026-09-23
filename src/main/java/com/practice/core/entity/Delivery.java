package com.practice.core.entity;

import com.practice.core.execption.BusinessValidationException;
import com.practice.core.model.DeliveryStatus;
import com.practice.core.model.ErrorCode;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String vehicleId;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private Instant startedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    @Column
    private Instant finishedAt;

    public static Delivery create(
            String vehicleId, String address, Instant startedAt, DeliveryStatus status, Instant finishedAt) {
        Delivery delivery = new Delivery();
        delivery.vehicleId = vehicleId;
        delivery.address = address;
        delivery.startedAt = startedAt;
        delivery.status = status;
        delivery.finishedAt = finishedAt;
        delivery.validateState();
        return delivery;
    }

    private void validateState() {

        if (vehicleId == null || vehicleId.isBlank()) {
            throw new BusinessValidationException(ErrorCode.VEHICLE_ID_REQUIRED, "vehicleId required");
        }
        if (address == null || address.isBlank()) {
            throw new BusinessValidationException(ErrorCode.ADDRESS_REQUIRED, "address required");
        }
        if (startedAt == null) {
            throw new BusinessValidationException(ErrorCode.STARTED_AT_REQUIRED, "startedAt at required");
        }
        if (status == null) {
            throw new BusinessValidationException(ErrorCode.STATUS_REQUIRED, "Delivery status is required");
        }
        if (status == DeliveryStatus.DELIVERED && finishedAt == null) {
            throw new BusinessValidationException(
                    ErrorCode.DELIVERY_FINISHED_AT_REQUIRED, "finishedAt is required when status is DELIVERED");
        }

        if (status == DeliveryStatus.IN_PROGRESS && finishedAt != null) {
            throw new BusinessValidationException(
                    ErrorCode.DELIVERY_FINISHED_AT_NOT_ALLOWED, "finishedAt must be null when status is IN_PROGRESS");
        }

        if (finishedAt != null && finishedAt.isBefore(startedAt)) {
            throw new BusinessValidationException(
                    ErrorCode.FINISHED_AT_BEFORE_STARTED_AT, "finishedAt cannot be before startedAt");
        }
    }
}
