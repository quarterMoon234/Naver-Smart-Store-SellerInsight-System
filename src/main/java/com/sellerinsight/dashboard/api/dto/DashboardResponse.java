package com.sellerinsight.dashboard.api.dto;

import com.sellerinsight.insight.api.dto.InsightResponse;
import com.sellerinsight.metric.api.dto.DailyMetricResponse;

import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(
        Long sellerId,
        String sellerName,
        LocalDate latestMetricDate,
        DailyMetricResponse latestMetric,
        int metricTrendDays,
        List<DashboardMetricPointResponse> metricTrend,
        DashboardInsightSummaryResponse insightSummary,
        int recentInsightCount,
        List<InsightResponse> recentInsights,
        SellerPipelineStatusResponse latestPipelineStatus
) {
}
