package com.sellerinsight.pipeline.domain;

import com.sellerinsight.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "pipeline_execution_locks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pipeline_execution_locks_pipeline_type_metric_date",
                        columnNames = {"pipeline_type", "metric_date"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PipelineExecutionLock extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_type", nullable = false, length = 30)
    private PipelineType pipelineType;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    private PipelineExecutionLock(PipelineType pipelineType, LocalDate metricDate) {
        this.pipelineType = pipelineType;
        this.metricDate = metricDate;
    }

    public static PipelineExecutionLock create(
            PipelineType pipelineType,
            LocalDate metricDate
    ) {
        return new PipelineExecutionLock(pipelineType, metricDate);
    }
}
