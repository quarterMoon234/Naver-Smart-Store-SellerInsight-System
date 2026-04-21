package com.sellerinsight.pipeline.domain;

import com.sellerinsight.common.entity.BaseEntity;
import com.sellerinsight.seller.domain.Seller;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
@Entity
@Table(name = "pipeline_run_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PipelineRunItem extends BaseEntity {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "pipeline_run_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pipeline_run_items_pipeline_run_id")
    )
    private PipelineRun pipelineRun;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "seller_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pipeline_run_items_seller_id")
    )
    private Seller seller;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PipelineRunItemStatus status;

    @Column(name = "generated_insight_count", nullable = false)
    private int generatedInsightCount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private OffsetDateTime endedAt;

    private PipelineRunItem(
            PipelineRun pipelineRun,
            Seller seller,
            PipelineRunItemStatus status,
            int generatedInsightCount,
            String errorMessage,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt
    ) {
        this.pipelineRun = pipelineRun;
        this.seller = seller;
        this.status = status;
        this.generatedInsightCount = generatedInsightCount;
        this.errorMessage = truncate(errorMessage);
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public static PipelineRunItem success(
            PipelineRun pipelineRun,
            Seller seller,
            int generatedInsightCount
    ) {
        OffsetDateTime now = OffsetDateTime.now(ASIA_SEOUL);

        return success(
                pipelineRun,
                seller,
                generatedInsightCount,
                now,
                now
        );
    }

    public static PipelineRunItem success(
            PipelineRun pipelineRun,
            Seller seller,
            int generatedInsightCount,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt
    ) {
        return new PipelineRunItem(
                pipelineRun,
                seller,
                PipelineRunItemStatus.SUCCESS,
                generatedInsightCount,
                null,
                startedAt,
                endedAt
        );
    }

    public static PipelineRunItem failed(
            PipelineRun pipelineRun,
            Seller seller,
            String errorMessage
    ) {
        OffsetDateTime now = OffsetDateTime.now(ASIA_SEOUL);

        return failed(
                pipelineRun,
                seller,
                errorMessage,
                now,
                now
        );
    }

    public static PipelineRunItem failed(
            PipelineRun pipelineRun,
            Seller seller,
            String errorMessage,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt
    ) {
        return new PipelineRunItem(
                pipelineRun,
                seller,
                PipelineRunItemStatus.FAILED,
                0,
                errorMessage,
                startedAt,
                endedAt
        );
    }

    public Long getDurationMs() {
        if (startedAt == null || endedAt == null) {
            return null;
        }

        return Duration.between(startedAt, endedAt).toMillis();
    }

    private static String truncate(String message) {
        if (message == null) {
            return null;
        }

        if (message.length() <= 1000) {
            return message;
        }

        return message.substring(0, 1000);
    }
}
