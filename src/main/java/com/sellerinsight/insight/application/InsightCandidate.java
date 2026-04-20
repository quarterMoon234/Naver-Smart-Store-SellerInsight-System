package com.sellerinsight.insight.application;

import com.sellerinsight.insight.domain.InsightSeverity;
import com.sellerinsight.insight.domain.InsightType;

import java.util.List;
import java.util.Map;

public record InsightCandidate(
        InsightType insightType,
        InsightSeverity severity,
        String title,
        String summary,
        Map<String, Object> evidence,
        List<RecommendationCandidate> recommendations,
        String generatedBy
) {
}
