package com.sellerinsight.pipeline.domain;

import com.sellerinsight.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
@Entity
@Table(name = "pipeline_runs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PipelineRun extends BaseEntity {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_type", nullable = false, length = 30)
    private PipelineType pipelineType;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 30)
    private PipelineTriggerType triggerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PipelineRunStatus status;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "total_seller_count", nullable = false)
    private int totalSellerCount;

    @Column(name = "processed_seller_count", nullable = false)
    private int processedSellerCount;

    @Column(name = "failed_seller_count", nullable = false)
    private int failedSellerCount;

    @Column(name = "generated_insight_count", nullable = false)
    private int generatedInsightCount;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    private PipelineRun(
            PipelineType pipelineType,
            PipelineTriggerType triggerType,
            LocalDate metricDate
    ) {
        this.pipelineType = pipelineType;
        this.triggerType = triggerType;
        this.status = PipelineRunStatus.RUNNING;
        this.metricDate = metricDate;
        this.totalSellerCount = 0;
        this.processedSellerCount = 0;
        this.failedSellerCount = 0;
        this.generatedInsightCount = 0;
        this.startedAt = OffsetDateTime.now(ASIA_SEOUL);
    }

    public static PipelineRun start(
            PipelineType pipelineType,
            PipelineTriggerType triggerType,
            LocalDate metricDate
    ) {
        return new PipelineRun(pipelineType, triggerType, metricDate);
    }

    public void complete(
            PipelineRunStatus status,
            int totalSellerCount,
            int processedSellerCount,
            int failedSellerCount,
            int generatedInsightCount
    ) {
        this.status = status;
        this.totalSellerCount = totalSellerCount;
        this.processedSellerCount = processedSellerCount;
        this.failedSellerCount = failedSellerCount;
        this.generatedInsightCount = generatedInsightCount;
        this.endedAt = OffsetDateTime.now(ASIA_SEOUL);
    }

    public Long getDurationMs() {
        if (startedAt == null || endedAt == null) {
            return null;
        }

        return Duration.between(startedAt, endedAt).toMillis();
    }
}
