package com.sellerinsight.dashboard.api.dto;

import java.time.LocalDate;
import java.util.List;

public record DashboardMetricRangeResponse(
        Long sellerId,
        LocalDate from,
        LocalDate to,
        int metricCount,
        List<DashboardMetricPointResponse> metrics
) {
}
