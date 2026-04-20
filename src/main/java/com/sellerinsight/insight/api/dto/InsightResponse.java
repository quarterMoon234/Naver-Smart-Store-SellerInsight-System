package com.sellerinsight.insight.api.dto;

import com.sellerinsight.insight.domain.Insight;
import com.sellerinsight.insight.domain.InsightSeverity;
import com.sellerinsight.insight.domain.InsightType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record InsightResponse(
        Long id,
        Long sellerId,
        LocalDate metricDate,
        InsightType insightType,
        InsightSeverity severity,
        String title,
        String summary,
        String evidenceJson,
        String generatedBy,
        List<RecommendationResponse> recommendations,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static InsightResponse from(Insight insight, List<RecommendationResponse> recommendations) {
        return new InsightResponse(
                insight.getId(),
                insight.getSeller().getId(),
                insight.getMetricDate(),
                insight.getInsightType(),
                insight.getSeverity(),
                insight.getTitle(),
                insight.getSummary(),
                insight.getEvidenceJson(),
                insight.getGeneratedBy(),
                recommendations,
                insight.getCreatedAt(),
                insight.getUpdatedAt()
        );
    }
}
