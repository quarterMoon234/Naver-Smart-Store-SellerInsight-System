package com.sellerinsight.pipeline.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PipelineExecutionLockRepository extends JpaRepository<PipelineExecutionLock, Long> {

    Optional<PipelineExecutionLock> findByPipelineTypeAndMetricDate(
            PipelineType pipelineType,
            LocalDate metricDate
    );

    long deleteByPipelineTypeAndMetricDate(
            PipelineType pipelineType,
            LocalDate metricDate
    );
}
