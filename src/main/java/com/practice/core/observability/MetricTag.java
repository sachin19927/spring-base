package com.practice.core.observability;

public record MetricTag(MetricTagKey key, String value) {
    public static MetricTag of(MetricTagKey key, String value) {
        return new MetricTag(key, value);
    }
}
