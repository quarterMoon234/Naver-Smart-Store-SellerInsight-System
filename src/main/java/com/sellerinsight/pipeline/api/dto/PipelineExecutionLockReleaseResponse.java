package com.sellerinsight.pipeline.api.dto;

import java.time.LocalDate;

public record PipelineExecutionLockReleaseResponse(
        LocalDate metricDate,
        boolean released
) {
}
