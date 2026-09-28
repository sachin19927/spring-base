package com.practice.core.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.practice.core.execption.BusinessValidationException;
import com.practice.core.model.DeliveryStatus;
import com.practice.core.model.ErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Delivery Entity Tests")
public class DeliveryTest {

    private static final String VEHICLE_ID = "VH-123-4";
    private static final String ADDRESS = "Nadeermerstrat 13";
    private static final Instant STARTED_AT = Instant.parse("2024-01-01T10:00:00Z");
    private static final Instant FINISHED_AT = Instant.parse("2024-01-01T12:00:00Z");

    @Test
    @DisplayName("create() should succeed with valid IN_PROGRESS delivery")
    void testCreateSucceedsWhenInProgressAndFinishedAtNull() {
        Delivery delivery = Delivery.create(VEHICLE_ID, ADDRESS, STARTED_AT, DeliveryStatus.IN_PROGRESS, null);

        assertThat(delivery.getVehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(delivery.getAddress()).isEqualTo(ADDRESS);
        assertThat(delivery.getStartedAt()).isEqualTo(STARTED_AT);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.IN_PROGRESS);
        assertThat(delivery.getFinishedAt()).isNull();
    }

    @Test
    @DisplayName("create() should succeed with valid DELIVERED delivery")
    void testCreateSucceedsWhenDeliveredAndFinishedAtPresent() {
        Delivery delivery = Delivery.create(VEHICLE_ID, ADDRESS, STARTED_AT, DeliveryStatus.DELIVERED, FINISHED_AT);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(delivery.getFinishedAt()).isEqualTo(FINISHED_AT);
    }

    @ParameterizedTest(name = "vehicleId=\"{0}\", address=\"{1}\", status={2} should throw BusinessValidationException")
    @DisplayName("create() should fail when a required field is null or blank")
    @CsvSource(
            nullValues = "null",
            value = {
                "'',    Nadeermerstrat 13, IN_PROGRESS",
                "null,    Nadeermerstrat 13, IN_PROGRESS",
                "VH-123-4,    '', IN_PROGRESS",
                "VH-123-4,    null, IN_PROGRESS",
                "VH-123-4,    Nadeermerstrat 13, null"
            })
    void testCreateFailsWithMissingRequiredFields(String vehicleId, String address, DeliveryStatus status) {
        assertThatThrownBy(() -> Delivery.create(vehicleId, address, STARTED_AT, status, null))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("create() should fail when startedAt is null")
    void testCreateFailsWhenStartedAtNull() {
        assertThatThrownBy(() -> Delivery.create(VEHICLE_ID, ADDRESS, null, DeliveryStatus.IN_PROGRESS, null))
                .isInstanceOf(BusinessValidationException.class)
                .extracting(ex -> ((BusinessValidationException) ex).getErrorCode())
                .isEqualTo(ErrorCode.STARTED_AT_REQUIRED);
    }

    @Test
    @DisplayName("create() should fail when status is DELIVERED but finishedAt is missing")
    void testCreateFailsWhenDeliveredWithoutFinishedAt() {
        assertThatThrownBy(() -> Delivery.create(VEHICLE_ID, ADDRESS, STARTED_AT, DeliveryStatus.DELIVERED, null))
                .isInstanceOf(BusinessValidationException.class)
                .extracting(ex -> ((BusinessValidationException) ex).getErrorCode())
                .isEqualTo(ErrorCode.DELIVERY_FINISHED_AT_REQUIRED);
    }

    @Test
    @DisplayName("create() should fail when status is IN_PROGRESS but finishedAt is present")
    void testCreateFailsWhenInProgressWithFinishedAt() {

        assertThatThrownBy(
                        () -> Delivery.create(VEHICLE_ID, ADDRESS, STARTED_AT, DeliveryStatus.IN_PROGRESS, FINISHED_AT))
                .isInstanceOf(BusinessValidationException.class)
                .extracting(ex -> ((BusinessValidationException) ex).getErrorCode())
                .isEqualTo(ErrorCode.DELIVERY_FINISHED_AT_NOT_ALLOWED);
    }

    @Test
    @DisplayName("create() should fail when finishedAt is before startedAt")
    void testCreateFailsWhenFinishedAtIsBeforeStartedAt() {
        Instant beforeStart = STARTED_AT.minusSeconds(3600);

        assertThatThrownBy(
                        () -> Delivery.create(VEHICLE_ID, ADDRESS, STARTED_AT, DeliveryStatus.DELIVERED, beforeStart))
                .isInstanceOf(BusinessValidationException.class)
                .extracting(ex -> ((BusinessValidationException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FINISHED_AT_BEFORE_STARTED_AT);
    }
}
