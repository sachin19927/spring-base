package com.practice.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.practice.core.dto.DeliveryRequest;
import com.practice.core.dto.DeliveryResponse;
import com.practice.core.execption.ResourceNotFoundException;
import com.practice.core.model.DeliveryStatus;
import com.practice.core.model.ErrorCode;
import com.practice.core.observability.MetricsRecorder;
import com.practice.core.service.DeliveryService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(DeliveryController.class)
@DisplayName("DeliveryController web layer Test")
public class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeliveryService deliveryService;

    @MockitoBean
    private MetricsRecorder recorder;

    private UUID deliveryId;
    private Instant startedAt;
    private DeliveryRequest validRequest;
    private DeliveryResponse responseDto;

    @BeforeEach
    void setUp() {
        deliveryId = UUID.randomUUID();
        startedAt = Instant.parse("2026-01-01T10:00:00Z");
        validRequest =
                new DeliveryRequest("VH-123-4", "Nadeermerstrat 13", startedAt, DeliveryStatus.IN_PROGRESS, null);
        responseDto = new DeliveryResponse(
                deliveryId, "VH-123-4", "Nadeermerstrat 13", startedAt, DeliveryStatus.IN_PROGRESS, null);
    }

    @Test
    @DisplayName("Post /v1/deliveries should return 201 created with delivery response")
    void testCreateDeliverySuccess() throws Exception {
        // Arrange
        when(deliveryService.createDelivery(any(DeliveryRequest.class))).thenReturn(responseDto);

        // Act
        mockMvc.perform(post("/v1/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(deliveryId.toString()))
                .andExpect(jsonPath("$.vehicleId").value("VH-123-4"))
                .andExpect(jsonPath("$.address").value("Nadeermerstrat 13"))
                .andExpect(jsonPath("$.status").value(DeliveryStatus.IN_PROGRESS.toString()));

        // Assert
        verify(deliveryService).createDelivery(any(DeliveryRequest.class));
    }

    @Test
    @DisplayName("Post /v1/deliveries should return 400 when vehicle id is missing")
    void testCreateDeliveryFailsValidationWhenVehicleIdBlank() throws Exception {
        // Arrange
        DeliveryRequest invalidRequest =
                new DeliveryRequest("", "Nadeermerstrat 13", startedAt, DeliveryStatus.IN_PROGRESS, null);

        // Act
        mockMvc.perform(post("/v1/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Assert
        verify(deliveryService, never()).createDelivery(any(DeliveryRequest.class));
    }

    @Test
    @DisplayName("Get /v1/deliveries/{id} should return 400 when startedAt is missing")
    void testCreateDeliveryFailsValidationWhenStartedAtMissing() throws Exception {
        // Arrange
        DeliveryRequest invalidRequest =
                new DeliveryRequest("VH-123-4", "Nadeermerstrat 13", null, DeliveryStatus.IN_PROGRESS, null);

        // Act
        mockMvc.perform(post("/v1/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Assert
        verify(deliveryService, never()).createDelivery(any(DeliveryRequest.class));
    }

    @Test
    @DisplayName("Get /v1/deliveries should return 400 when request body is malformed")
    void testCreateDeliveryFailsWhenBodyMalformed() throws Exception {
       //Act
        mockMvc.perform(post("/v1/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        // Assert
        verify(deliveryService, never()).createDelivery(any(DeliveryRequest.class));
    }

    @Test
    @DisplayName("Get /v1/deliveries/{id} should return 200 when delivery response")
    void testGetDeliverySuccess() throws Exception {

        //Arrange
        when(deliveryService.getDelivery(eq(deliveryId))).thenReturn(responseDto);

        //Act
        mockMvc.perform(get("/v1/deliveries/{id}", deliveryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deliveryId.toString()))
                .andExpect(jsonPath("$.vehicleId").value("VH-123-4"))
                .andExpect(jsonPath("$.address").value("Nadeermerstrat 13"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        //Assert
        verify(deliveryService).getDelivery(deliveryId);
    }

    @Test
    @DisplayName("Get /v1/deliveries/{id} should return 400 when delivery not found")
    void testGetDeliveryNotFound() throws Exception {

        //Arrange
        UUID missingId = UUID.randomUUID();
        when(deliveryService.getDelivery(eq(missingId)))
                .thenThrow(new ResourceNotFoundException(ErrorCode.DELIVERY_NOT_FOUND,"Delivery not found"));

        //Act
        mockMvc.perform(get("/v1/deliveries/{id}", missingId))
                .andExpect(status().isBadRequest());

        //Assert
        verify(deliveryService).getDelivery(missingId);
    }

    @Test
    @DisplayName("Get /v1/deliveries/{id} should return 400 when id is not a valid UUID")
    void testGetDeliveryFailsWhenIdInvalid() throws Exception {

        //Act
        mockMvc.perform(get("/v1/deliveries/{id}", "not-a-valid-uuid"))
                .andExpect(status().isBadRequest());

        //Assert
        verify(deliveryService,never()).getDelivery(any());
    }
}
