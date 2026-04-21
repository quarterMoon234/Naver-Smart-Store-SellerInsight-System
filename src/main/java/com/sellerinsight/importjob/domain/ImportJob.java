package com.sellerinsight.importjob.domain;

import com.sellerinsight.common.entity.BaseEntity;
import com.sellerinsight.seller.domain.Seller;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
@Entity
@Table(name = "import_jobs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImportJob extends BaseEntity {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "seller_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_import_jobs_seller_id")
    )
    private Seller seller;

    @Enumerated(EnumType.STRING)
    @Column(name = "import_type", nullable = false, length = 30)
    private ImportJobType importType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ImportJobStatus status;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "total_row_count", nullable = false)
    private int totalRowCount;

    @Column(name = "success_row_count", nullable = false)
    private int successRowCount;

    @Column(name = "failed_row_count", nullable = false)
    private int failedRowCount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    private ImportJob(Seller seller, ImportJobType importType, String originalFileName) {
        this.seller = seller;
        this.importType = importType;
        this.status = ImportJobStatus.PENDING;
        this.originalFileName = originalFileName;
        this.totalRowCount = 0;
        this.successRowCount = 0;
        this.failedRowCount = 0;
    }

    public static ImportJob create(
            Seller seller,
            ImportJobType importType,
            String originalFileName
    ) {
        return new ImportJob(seller, importType, originalFileName);
    }

    public void markProcessing() {
        this.status = ImportJobStatus.PROCESSING;
        this.startedAt = OffsetDateTime.now(ASIA_SEOUL);
        this.errorMessage = null;
    }

    public void markSuccess(int totalRowCount, int successRowCount, int failedRowCount) {
        this.status = ImportJobStatus.SUCCESS;
        this.totalRowCount = totalRowCount;
        this.successRowCount = successRowCount;
        this.failedRowCount = failedRowCount;
        this.endedAt = OffsetDateTime.now(ASIA_SEOUL);
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        markFailed(this.totalRowCount, this.successRowCount, this.failedRowCount, errorMessage);
    }

    public void markFailed(
            int totalRowCount,
            int successRowCount,
            int failedRowCount,
            String errorMessage
    ) {
        this.status = ImportJobStatus.FAILED;
        this.totalRowCount = totalRowCount;
        this.successRowCount = successRowCount;
        this.failedRowCount = failedRowCount;
        this.endedAt = OffsetDateTime.now(ASIA_SEOUL);
        this.errorMessage = truncate(errorMessage);
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }

        if (message.length() <= 1000) {
            return message;
        }

        return message.substring(0, 1000);
    }
}
