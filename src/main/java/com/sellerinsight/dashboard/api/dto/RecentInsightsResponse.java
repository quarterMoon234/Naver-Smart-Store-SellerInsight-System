package com.sellerinsight.dashboard.api.dto;

import com.sellerinsight.insight.api.dto.InsightResponse;

import java.util.List;

public record RecentInsightsResponse(
        Long sellerId,
        int limit,
        int insightCount,
        List<InsightResponse> insights
) {
}
