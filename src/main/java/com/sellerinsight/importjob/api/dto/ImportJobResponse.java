package com.sellerinsight.importjob.api.dto;

import com.sellerinsight.importjob.domain.ImportJob;
import com.sellerinsight.importjob.domain.ImportJobStatus;
import com.sellerinsight.importjob.domain.ImportJobType;

import java.time.OffsetDateTime;

public record ImportJobResponse(
        Long id,
        Long sellerId,
        ImportJobType importType,
        ImportJobStatus status,
        String originalFileName,
        int totalRowCount,
        int successRowCount,
        int failedRowCount,
        String errorMessage,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ImportJobResponse from(ImportJob importJob) {
        return new ImportJobResponse(
                importJob.getId(),
                importJob.getSeller().getId(),
                importJob.getImportType(),
                importJob.getStatus(),
                importJob.getOriginalFileName(),
                importJob.getTotalRowCount(),
                importJob.getSuccessRowCount(),
                importJob.getFailedRowCount(),
                importJob.getErrorMessage(),
                importJob.getStartedAt(),
                importJob.getEndedAt(),
                importJob.getCreatedAt(),
                importJob.getUpdatedAt()
        );
    }
}
