package com.sellerinsight.insight.api.dto;

import com.sellerinsight.insight.domain.Recommendation;

import java.time.OffsetDateTime;

public record RecommendationResponse(
        Long id,
        int priority,
        String actionCode,
        String actionTitle,
        String actionMessage,
        String sourceRule,
        OffsetDateTime createdAt
) {
    public static RecommendationResponse from(Recommendation recommendation) {
        return new RecommendationResponse(
                recommendation.getId(),
                recommendation.getPriority(),
                recommendation.getActionCode(),
                recommendation.getActionTitle(),
                recommendation.getActionMessage(),
                recommendation.getSourceRule(),
                recommendation.getCreatedAt()
        );
    }
}
