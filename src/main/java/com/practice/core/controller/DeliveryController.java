package com.practice.core.controller;

import com.practice.core.dto.DeliveryRequest;
import com.practice.core.dto.DeliveryResponse;
import com.practice.core.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelviery(@Valid @RequestBody DeliveryRequest deliveryRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryService.createDelivery(deliveryRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDelivery(@PathVariable UUID id){
        return ResponseEntity.ok(deliveryService.getDelivery(id));
    }
}
