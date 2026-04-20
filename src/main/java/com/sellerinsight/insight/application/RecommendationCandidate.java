package com.sellerinsight.insight.application;

public record RecommendationCandidate(
        int priority,
        String actionCode,
        String actionTitle,
        String actionMessage
) {
}
