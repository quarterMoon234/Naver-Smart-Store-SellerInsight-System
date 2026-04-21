package com.sellerinsight.pipeline.api.dto;

import com.sellerinsight.pipeline.domain.PipelineRun;
import com.sellerinsight.pipeline.domain.PipelineRunStatus;
import com.sellerinsight.pipeline.domain.PipelineTriggerType;
import com.sellerinsight.pipeline.domain.PipelineType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record DailyPipelineRunResponse(
        Long runId,
        PipelineType pipelineType,
        PipelineTriggerType triggerType,
        PipelineRunStatus status,
        LocalDate metricDate,
        int totalSellerCount,
        int processedSellerCount,
        int failedSellerCount,
        int generatedInsightCount,
        List<Long> failedSellerIds,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        Long durationMs
) {
    public static DailyPipelineRunResponse from(
            PipelineRun pipelineRun,
            List<Long> failedSellerIds
    ) {
        return new DailyPipelineRunResponse(
                pipelineRun.getId(),
                pipelineRun.getPipelineType(),
                pipelineRun.getTriggerType(),
                pipelineRun.getStatus(),
                pipelineRun.getMetricDate(),
                pipelineRun.getTotalSellerCount(),
                pipelineRun.getProcessedSellerCount(),
                pipelineRun.getFailedSellerCount(),
                pipelineRun.getGeneratedInsightCount(),
                failedSellerIds,
                pipelineRun.getStartedAt(),
                pipelineRun.getEndedAt(),
                pipelineRun.getDurationMs()
        );
    }
}
