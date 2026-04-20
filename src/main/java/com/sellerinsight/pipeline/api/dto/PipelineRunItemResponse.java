package com.sellerinsight.pipeline.api.dto;

import com.sellerinsight.pipeline.domain.PipelineRunItem;
import com.sellerinsight.pipeline.domain.PipelineRunItemStatus;

import java.time.OffsetDateTime;

public record PipelineRunItemResponse(
        Long id,
        Long sellerId,
        PipelineRunItemStatus status,
        int generatedInsightCount,
        String errorMessage,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt
) {
    public static PipelineRunItemResponse from(PipelineRunItem pipelineRunItem) {
        return new PipelineRunItemResponse(
                pipelineRunItem.getId(),
                pipelineRunItem.getSeller().getId(),
                pipelineRunItem.getStatus(),
                pipelineRunItem.getGeneratedInsightCount(),
                pipelineRunItem.getErrorMessage(),
                pipelineRunItem.getStartedAt(),
                pipelineRunItem.getEndedAt()
        );
    }
}
