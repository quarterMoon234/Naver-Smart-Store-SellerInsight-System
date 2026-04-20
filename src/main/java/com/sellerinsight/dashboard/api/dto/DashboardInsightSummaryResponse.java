package com.sellerinsight.dashboard.api.dto;

public record DashboardInsightSummaryResponse(
        long totalCount,
        long lowCount,
        long mediumCount,
        long highCount
) {
    public static DashboardInsightSummaryResponse of(
            long lowCount,
            long mediumCount,
            long highCount
    ) {
        return new DashboardInsightSummaryResponse(
                lowCount + mediumCount + highCount,
                lowCount,
                mediumCount,
                highCount
        );
    }
}
