package com.sellerinsight.insight.api.dto;

import java.time.LocalDate;
import java.util.List;

public record InsightsByDateResponse(
        Long sellerId,
        LocalDate metricDate,
        int insightCount,
        List<InsightResponse> insights
) {
}
