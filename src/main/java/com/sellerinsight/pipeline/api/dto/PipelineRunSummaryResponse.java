package com.sellerinsight.pipeline.api.dto;

import com.sellerinsight.pipeline.domain.PipelineRun;
import com.sellerinsight.pipeline.domain.PipelineRunStatus;
import com.sellerinsight.pipeline.domain.PipelineTriggerType;
import com.sellerinsight.pipeline.domain.PipelineType;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PipelineRunSummaryResponse(
        Long id,
        PipelineType pipelineType,
        PipelineTriggerType triggerType,
        PipelineRunStatus status,
        LocalDate metricDate,
        int totalSellerCount,
        int processedSellerCount,
        int failedSellerCount,
        int generatedInsightCount,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        Long durationMs
) {
    public static PipelineRunSummaryResponse from(PipelineRun pipelineRun) {
        return new PipelineRunSummaryResponse(
                pipelineRun.getId(),
                pipelineRun.getPipelineType(),
                pipelineRun.getTriggerType(),
                pipelineRun.getStatus(),
                pipelineRun.getMetricDate(),
                pipelineRun.getTotalSellerCount(),
                pipelineRun.getProcessedSellerCount(),
                pipelineRun.getFailedSellerCount(),
                pipelineRun.getGeneratedInsightCount(),
                pipelineRun.getStartedAt(),
                pipelineRun.getEndedAt(),
                pipelineRun.getDurationMs()
        );
    }
}
