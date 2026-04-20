package com.sellerinsight.dashboard.api.dto;

import com.sellerinsight.metric.domain.DailyMetric;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardMetricPointResponse(
        LocalDate metricDate,
        int orderCount,
        BigDecimal salesAmount,
        BigDecimal averageOrderAmount,
        int soldOutProductCount,
        int staleProductCount
) {
    public static DashboardMetricPointResponse from(DailyMetric dailyMetric) {
        return new DashboardMetricPointResponse(
                dailyMetric.getMetricDate(),
                dailyMetric.getOrderCount(),
                dailyMetric.getSalesAmount(),
                dailyMetric.getAverageOrderAmount(),
                dailyMetric.getSoldOutProductCount(),
                dailyMetric.getStaleProductCount()
        );
    }
}
