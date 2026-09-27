package com.practice.core.observability;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MetricTagKey {
    ERROR_TYPE("error_type"),
    REASON("reason"),
    OUTCOME("outcome"),
    EXCEPTION("exception"),
    STATUS("status");

    private final String key;
}

