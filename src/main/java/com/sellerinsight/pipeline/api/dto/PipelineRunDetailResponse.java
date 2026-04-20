package com.sellerinsight.pipeline.api.dto;

import java.util.List;

public record PipelineRunDetailResponse(
        PipelineRunSummaryResponse run,
        int itemCount,
        List<PipelineRunItemResponse> items
) {
}
