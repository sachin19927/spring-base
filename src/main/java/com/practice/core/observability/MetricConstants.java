package com.practice.core.observability;

import lombok.Getter;

@Getter
public enum MetricConstants implements MetricDefinition {
    DELIVERY_CREATE("delivery.created.total", "Number of Delivery request created"),
    DELIVERY_IN_PROGRESS("delivery.created.inprogress.total", "Number of Delivery request in-progress created"),
    DELIVERY_DELIVERED("delivery.created.delivered.total", "Number of Delivery request delivered"),
    PROCESS_COMPLETION_DURATION("process.completion.duration", "Time taken for process completion"),
    API_ERROR("api.errors.total", "Number of API errors returned to clients, tagged by error code"),
OUTCOME_FAILURE("delivery.created.failure","Outcome Failed");

    private final String metricName;
    private final String description;

    MetricConstants(String metricName, String description) {
        this.metricName = metricName;
        this.description = description;
    }
}
