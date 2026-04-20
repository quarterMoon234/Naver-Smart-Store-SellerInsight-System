package com.sellerinsight.pipeline.api.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyPipelineRunResponse(
        LocalDate metricDate,
        int totalSellerCount,
        int processedSellerCount,
        int failedSellerCount,
        int generatedInsightCount,
        List<Long> failedSellerIds
) {
}
