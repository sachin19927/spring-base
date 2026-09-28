package com.practice.core.service;

import com.practice.core.dto.DeliveryRequest;
import com.practice.core.dto.DeliveryResponse;
import com.practice.core.entity.Delivery;
import com.practice.core.execption.BusinessValidationException;
import com.practice.core.execption.ResourceNotFoundException;
import com.practice.core.mapper.DeliveryMapper;
import com.practice.core.model.DeliveryStatus;
import com.practice.core.model.ErrorCode;
import com.practice.core.observability.*;
import com.practice.core.repository.DeliveryRepository;
import io.micrometer.core.instrument.Timer;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class DeliveryService {

    private final DeliveryMapper deliveryMapper;
    private final DeliveryRepository deliveryRepository;
    private final Clock clock;
    private final MetricsRecorder recorder;

    @Transactional
    public DeliveryResponse createDelivery(DeliveryRequest deliveryRequest) {
        Timer.Sample sample = recorder.startTimer();
        try {
            validateDelivery(deliveryRequest);
            Delivery delivery = Delivery.create(
                    deliveryRequest.vehicleId(),
                    deliveryRequest.address(),
                    deliveryRequest.startedAt(),
                    deliveryRequest.status(),
                    deliveryRequest.finishedAt());
            Delivery savedDelivery = deliveryRepository.save(delivery);
            DeliveryResponse response = deliveryMapper.toResponse(savedDelivery);

            recordCreationSuccess(savedDelivery);
            recorder.stopTimer(
                    sample,
                    MetricConstants.PROCESS_COMPLETION_DURATION,
                    MetricTag.of(MetricTagKey.OUTCOME, MetricTagValue.OUTCOME_SUCCESS.name()));

            return response;
        } catch (RuntimeException ex) {
            recorder.stopTimer(
                    sample,
                    MetricConstants.PROCESS_COMPLETION_DURATION,
                    MetricTag.of(MetricTagKey.OUTCOME, MetricTagValue.OUTCOME_FAILURE.name()));
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getDelivery(UUID id) {
        Delivery delivery = deliveryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.DELIVERY_NOT_FOUND, "Delivery not found"));
        return deliveryMapper.toResponse(delivery);
    }

    private void validateDelivery(DeliveryRequest request) {
        if (request.startedAt() != null && request.startedAt().isAfter(Instant.now(clock))) {
            throw new BusinessValidationException(ErrorCode.STARTED_AT_IN_FUTURE, "Started at cannot be in the future");
        }
    }

    private void recordCreationSuccess(Delivery savedDelivery) {
        recorder.increment(
                MetricConstants.DELIVERY_CREATE,
                MetricTag.of(MetricTagKey.STATUS, savedDelivery.getStatus().name()),
                MetricTag.of(MetricTagKey.OUTCOME, MetricTagValue.OUTCOME_SUCCESS.name()));
        if (savedDelivery.getStatus() == DeliveryStatus.IN_PROGRESS) {
            recorder.increment(MetricConstants.DELIVERY_IN_PROGRESS);
        }
    }
}
