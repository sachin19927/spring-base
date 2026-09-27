package com.practice.core.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricsRecorder {

    private final MeterRegistry meterRegistry;

    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();

    public void increment(MetricDefinition definition, MetricTag... tags) {
        validateTags(tags);
        counters.computeIfAbsent(cacheKey(definition, tags), k -> {
                    Counter.Builder builder =
                            Counter.builder(definition.getMetricName()).description(definition.getDescription());
                    for (MetricTag tag : tags) {
                        builder = builder.tag(tag.key().getKey(), tag.value());
                    }
                    return builder.register(meterRegistry);
                })
                .increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, MetricDefinition definition, MetricTag... tags) {
        validateTags(tags);
        sample.stop(timers.computeIfAbsent(cacheKey(definition, tags), k -> {
            Timer.Builder builder = Timer.builder(definition.getMetricName())
                    .description(definition.getDescription())
                    .publishPercentileHistogram();
            for (MetricTag tag : tags) {
                builder = builder.tag(tag.key().getKey(), tag.value());
            }
            return builder.register(meterRegistry);
        }));
    }

    public void validateTags(MetricTag[] tags) {
        if (tags == null) {
            throw new IllegalArgumentException("Metric tags cannot be null");
        }
        for (MetricTag tag : tags) {
            if (tag == null) {
                throw new IllegalArgumentException("tags must not contain null elements");
            }
        }
    }

    private String cacheKey(MetricDefinition definition, MetricTag... tags) {
        MetricTag[] sorted = tags.clone();
        Arrays.sort(sorted, Comparator.comparing(tag -> tag.key().getKey()));

        StringBuilder key = new StringBuilder(definition.getMetricName());
        for (MetricTag tag : sorted) {
            key.append("|").append(tag.key().getKey()).append("=").append(tag.value());
        }
        return key.toString();
    }
}
