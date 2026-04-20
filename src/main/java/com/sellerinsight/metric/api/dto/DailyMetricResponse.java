package com.sellerinsight.metric.api.dto;

import com.sellerinsight.metric.domain.DailyMetric;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record DailyMetricResponse (
        Long id,
        Long sellerId,
        LocalDate metricDate,
        int orderCount,
        BigDecimal salesAmount,
        BigDecimal averageOrderAmount,
        int soldOutProductCount,
        int staleProductCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static DailyMetricResponse from(DailyMetric dailyMetric) {
        return new DailyMetricResponse(
                dailyMetric.getId(),
                dailyMetric.getSeller().getId(),
                dailyMetric.getMetricDate(),
                dailyMetric.getOrderCount(),
                dailyMetric.getSalesAmount(),
                dailyMetric.getAverageOrderAmount(),
                dailyMetric.getSoldOutProductCount(),
                dailyMetric.getStaleProductCount(),
                dailyMetric.getCreatedAt(),
                dailyMetric.getUpdatedAt()
        );
    }
}
