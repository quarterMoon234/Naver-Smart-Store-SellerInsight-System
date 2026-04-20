package com.sellerinsight.dashboard.api.dto;

import com.sellerinsight.pipeline.domain.PipelineRunItem;
import com.sellerinsight.pipeline.domain.PipelineRunItemStatus;
import com.sellerinsight.pipeline.domain.PipelineTriggerType;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record SellerPipelineStatusResponse(
        Long runId,
        LocalDate metricDate,
        PipelineTriggerType triggerType,
        PipelineRunItemStatus status,
        int generatedInsightCount,
        String errorMessage,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt
) {
    public static SellerPipelineStatusResponse from(PipelineRunItem pipelineRunItem) {
        return new SellerPipelineStatusResponse(
                pipelineRunItem.getPipelineRun().getId(),
                pipelineRunItem.getPipelineRun().getMetricDate(),
                pipelineRunItem.getPipelineRun().getTriggerType(),
                pipelineRunItem.getStatus(),
                pipelineRunItem.getGeneratedInsightCount(),
                pipelineRunItem.getErrorMessage(),
                pipelineRunItem.getStartedAt(),
                pipelineRunItem.getEndedAt()
        );
    }
}
