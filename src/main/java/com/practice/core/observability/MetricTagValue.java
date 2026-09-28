package com.practice.core.observability;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MetricTagValue {
    OUTCOME_SUCCESS("success"),
    OUTCOME_FAILURE("failure");

    private final String value;
}
