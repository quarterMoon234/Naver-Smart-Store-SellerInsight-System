package com.sellerinsight.insight.application;

import com.sellerinsight.metric.domain.DailyMetric;

import java.time.LocalDate;

public record MetricContext(
        Long sellerId,
        LocalDate metricDate,
        DailyMetric currentMetric,
        DailyMetric previousMetric
) {
}
