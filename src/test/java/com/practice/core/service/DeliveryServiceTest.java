package com.practice.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.practice.core.dto.DeliveryRequest;
import com.practice.core.dto.DeliveryResponse;
import com.practice.core.entity.Delivery;
import com.practice.core.execption.BusinessValidationException;
import com.practice.core.execption.ResourceNotFoundException;
import com.practice.core.mapper.DeliveryMapper;
import com.practice.core.model.DeliveryStatus;
import com.practice.core.observability.MetricsRecorder;
import com.practice.core.repository.DeliveryRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryService Unit Test")
public class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryMapper deliveryMapper;

    @Mock
    private Clock clock;

    @Mock
    private MetricsRecorder recorder;

    @InjectMocks
    private DeliveryService deliveryService;

    private UUID deliveryId;
    private DeliveryRequest validRequest;
    private DeliveryResponse responseDto;
    private Instant pastInstant;
    private Instant futureInstant;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        pastInstant = now.minusSeconds(3600);
        futureInstant = now.plusSeconds(3600);
        deliveryId = UUID.randomUUID();

        validRequest =
                new DeliveryRequest("VH-123-4", "Nadeermerstrat 13", pastInstant, DeliveryStatus.IN_PROGRESS, null);
        responseDto = new DeliveryResponse(
                deliveryId, "VH-123-4", "Nadeermerstrat 13", pastInstant, DeliveryStatus.IN_PROGRESS, null);
    }

    @Test
    @DisplayName("Should successfully create delivery when startedAt is in the past")
    void testCreateDeliverySuccess() {
        // Arrange
        Delivery savedDelivery =
                Delivery.create("VH-123-4", "Nadeermerstrat 13", pastInstant, DeliveryStatus.IN_PROGRESS, null);
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(savedDelivery);
        when(deliveryMapper.toResponse(any(Delivery.class))).thenReturn(responseDto);
        when(clock.instant()).thenReturn(pastInstant);

        // Act
        DeliveryResponse result = deliveryService.createDelivery(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals("VH-123-4", result.vehicleId());
        assertEquals("Nadeermerstrat 13", result.address());
        assertEquals(DeliveryStatus.IN_PROGRESS, result.status());
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
        verify(deliveryMapper, times(1)).toResponse(any(Delivery.class));
    }

    @Test
    @DisplayName("Should throw BusinessValidationException when startedAt is in the future")
    void testCreateDeliveryWithFutureStartedAt() {
        // Arrange
        DeliveryRequest futureRequest =
                new DeliveryRequest("VH-123-4", "Nadeermerstrat 13", futureInstant, DeliveryStatus.IN_PROGRESS, null);

        when(clock.instant()).thenReturn(now);

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> deliveryService.createDelivery(futureRequest));

        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    @DisplayName("Should successfully retrieve delivery by ID")
    void testGetDeliverySuccess() {
        // Arrange
        Delivery mockDelivery = new Delivery();
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(mockDelivery));
        when(deliveryMapper.toResponse(mockDelivery)).thenReturn(responseDto);

        // Act
        DeliveryResponse result = deliveryService.getDelivery(deliveryId);

        // Assert
        assertNotNull(result);
        assertEquals("VH-123-4", result.vehicleId());
        assertEquals("Nadeermerstrat 13", result.address());
        verify(deliveryRepository, times(1)).findById(deliveryId);
        verify(deliveryMapper, times(1)).toResponse(mockDelivery);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when delivery ID not found")
    void testDeliveryNotFound() {
        // Arrange
        UUID nonExistenceID = UUID.randomUUID();
        when(deliveryRepository.findById(nonExistenceID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> deliveryService.getDelivery(nonExistenceID));

        verify(deliveryRepository, times(1)).findById(nonExistenceID);
        verify(deliveryMapper, never()).toResponse(any(Delivery.class));
    }

    @ParameterizedTest(name = "Should fail when {0} is null")
    @CsvSource(
            nullValues = "null",
            value = {
                "vehicleId,null,Nadeermerstrat 13,IN_PROGRESS",
                "address,VH-123-4,null,IN_PROGRESS",
                "status,VH-123-4,Nadeermerstrat 13,null"
            })
    @DisplayName("Should throw BusinessValidationException for null required fields")
    void testDeliveryCreationFailsWithNullRequiredFields(
            String fieldName, String vehicleId, String address, DeliveryStatus status) {
        // Arrange
        DeliveryRequest invalidRequest = new DeliveryRequest(vehicleId, address, pastInstant, status, null);

        when(clock.instant()).thenReturn(futureInstant);

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> deliveryService.createDelivery(invalidRequest));
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    @DisplayName("Should throw BusinessValidationException when finishedAt is required for DELIVERED status")
    void testDeliveryRequiresFinishedAtForDeliveredStatus() {
        // Arrange
        DeliveryRequest invalidRequest =
                new DeliveryRequest("VH-123-4", "Nadeermerstrat 13", pastInstant, DeliveryStatus.DELIVERED, null);

        when(clock.instant()).thenReturn(futureInstant);

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> deliveryService.createDelivery(invalidRequest));
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    @DisplayName("Should throw BusinessValidationException when finishedAt is set for IN_PROGRESS status")
    void testDeliveryRejectsFinishedAtForInProgressStatus() {
        // Arrange
        DeliveryRequest invalidRequest = new DeliveryRequest(
                "VH-123-4", "Nadeermerstrat 13", pastInstant, DeliveryStatus.IN_PROGRESS, futureInstant);

        when(clock.instant()).thenReturn(futureInstant);

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> deliveryService.createDelivery(invalidRequest));
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    @DisplayName("Should throw BusinessValidationException when finishedAt is before startedAt")
    void testDeliveryRejectsFinishedAtBeforeStartedAt() {
        // Arrange
        Instant earlierInstant = pastInstant.minusSeconds(7200);
        DeliveryRequest invalidRequest = new DeliveryRequest(
                "VH-123-4", "Nadeermerstrat 13", pastInstant, DeliveryStatus.DELIVERED, earlierInstant);

        when(clock.instant()).thenReturn(futureInstant);

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> deliveryService.createDelivery(invalidRequest));
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    @DisplayName("Should successfully create delivery with DELIVERED status and valid finishedAt")
    void testCreateDeliveryWithDeliveredStatus() {
        // Arrange
        DeliveryRequest deliveryRequest = new DeliveryRequest(
                "VH-123-4", "Nadeermerstrat 13", pastInstant, DeliveryStatus.DELIVERED, futureInstant);
        Delivery savedDelivery =
                Delivery.create("VH-123-4", "Nadeermerstrat 13", pastInstant, DeliveryStatus.DELIVERED, futureInstant);
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(savedDelivery);

        DeliveryResponse deliveryResponse = new DeliveryResponse(
                deliveryId, "VH-123-4", "Nadeermerstrat 13", pastInstant, DeliveryStatus.DELIVERED, futureInstant);
        when(deliveryMapper.toResponse(savedDelivery)).thenReturn(deliveryResponse);
        when(clock.instant()).thenReturn(futureInstant.plusSeconds(3600));

        // Act
        DeliveryResponse result = deliveryService.createDelivery(deliveryRequest);

        // Assert
        assertNotNull(result);
        assertEquals(DeliveryStatus.DELIVERED, result.status());
        assertEquals(futureInstant, result.finishedAt());
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
    }

    @Test
    @DisplayName("Should validate that startedAt cannot be blank (null)")
    void testDeliveryCreationFailsWithNullStartedAt() {
        // Arrange
        DeliveryRequest invalidRequest =
                new DeliveryRequest("VH-123-4", "Nadeermerstrat 13", null, DeliveryStatus.IN_PROGRESS, null);

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> deliveryService.createDelivery(invalidRequest));
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    @DisplayName("Should validate that address is not blank/whitespace")
    void testDeliveryCreationFailsWithBlankAddress() {
        // Arrange
        DeliveryRequest invalidRequest =
                new DeliveryRequest("VH-123-4", "   ", pastInstant, DeliveryStatus.IN_PROGRESS, null);

        when(clock.instant()).thenReturn(futureInstant);

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> deliveryService.createDelivery(invalidRequest));
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }
}
