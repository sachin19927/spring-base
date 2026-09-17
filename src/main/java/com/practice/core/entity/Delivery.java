package com.practice.core.entity;

import com.practice.core.model.DeliveryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id =  UUID.randomUUID();

    @Column(nullable = false)
    private String vehicleId;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Instant startedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

}
