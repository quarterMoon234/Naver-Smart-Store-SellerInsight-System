package com.sellerinsight.pipeline.application;

import com.sellerinsight.common.error.BusinessException;
import com.sellerinsight.common.error.ErrorCode;
import com.sellerinsight.insight.api.dto.InsightsByDateResponse;
import com.sellerinsight.insight.application.InsightGenerationService;
import com.sellerinsight.metric.application.DailyMetricAggregationService;
import com.sellerinsight.pipeline.api.dto.*;
import com.sellerinsight.pipeline.domain.*;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerRepository;
import com.sellerinsight.seller.domain.SellerStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DailyPipelineExecutionService {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

    private final SellerRepository sellerRepository;
    private final DailyMetricAggregationService dailyMetricAggregationService;
    private final InsightGenerationService insightGenerationService;
    private final PipelineRunRepository pipelineRunRepository;
    private final PipelineRunItemRepository pipelineRunItemRepository;
    private final PipelineExecutionLockRepository pipelineExecutionLockRepository;
    private final PipelineMetricsRecorder pipelineMetricsRecorder;

    @Value("${app.pipeline.daily.lock-timeout-minutes:30}")
    private long lockTimeoutMinutes;

    public DailyPipelineRunResponse run(LocalDate metricDate) {
        return run(metricDate, PipelineTriggerType.MANUAL);
    }

    public DailyPipelineRunResponse runScheduled(LocalDate metricDate) {
        return run(metricDate, PipelineTriggerType.SCHEDULED);
    }

    public List<PipelineRunSummaryResponse> getRuns(int limit) {
        int safeLimit = Math.max(limit, 1);

        return pipelineRunRepository.findAllByPipelineTypeOrderByIdDesc(PipelineType.DAILY).stream()
                .limit(safeLimit)
                .map(PipelineRunSummaryResponse::from)
                .toList();
    }

    public PipelineRunDetailResponse getRunDetail(Long runId) {
        PipelineRun pipelineRun = pipelineRunRepository.findByIdAndPipelineType(runId, PipelineType.DAILY)
                .orElseThrow(() -> new BusinessException(ErrorCode.PIPELINE_RUN_NOT_FOUND));

        List<PipelineRunItemResponse> items = pipelineRunItemRepository.findAllByPipelineRunIdOrderByIdAsc(runId).stream()
                .map(PipelineRunItemResponse::from)
                .toList();

        return new PipelineRunDetailResponse(
                PipelineRunSummaryResponse.from(pipelineRun),
                items.size(),
                items
        );
    }

    public PipelineExecutionLockReleaseResponse forceReleaseLock(LocalDate metricDate) {
        long deleteCount = pipelineExecutionLockRepository.deleteByPipelineTypeAndMetricDate(
                PipelineType.DAILY,
                metricDate
        );

        return new PipelineExecutionLockReleaseResponse(metricDate, deleteCount > 0);
    }

    public DailyPipelineRunResponse run(LocalDate metricDate, PipelineTriggerType triggerType) {
        boolean lockAcquired = false;

        try {
            acquireExecutionLock(metricDate);
            lockAcquired = true;

            List<Seller> sellers = sellerRepository.findAllByStatus(SellerStatus.CONNECTED);

            PipelineRun pipelineRun = pipelineRunRepository.saveAndFlush(
                    PipelineRun.start(PipelineType.DAILY, triggerType, metricDate)
            );

            int processedSellerCount = 0;
            int failedSellerCount = 0;
            int generatedInsightCount = 0;
            List<Long> failedSellerIds = new ArrayList<>();

            for (Seller seller : sellers) {
                OffsetDateTime itemStartedAt = OffsetDateTime.now(ASIA_SEOUL);

                try {
                    dailyMetricAggregationService.aggregate(seller.getId(), metricDate);
                    InsightsByDateResponse insightResult = insightGenerationService.generate(
                            seller.getId(),
                            metricDate
                    );

                    processedSellerCount++;
                    generatedInsightCount += insightResult.insightCount();

                    OffsetDateTime itemEndedAt = OffsetDateTime.now(ASIA_SEOUL);

                    pipelineRunItemRepository.save(
                            PipelineRunItem.success(
                                    pipelineRun,
                                    seller,
                                    insightResult.insightCount(),
                                    itemStartedAt,
                                    itemEndedAt
                            )
                    );
                } catch (Exception exception) {
                    failedSellerCount++;
                    failedSellerIds.add(seller.getId());

                    OffsetDateTime itemEndedAt = OffsetDateTime.now(ASIA_SEOUL);

                    pipelineRunItemRepository.save(
                            PipelineRunItem.failed(
                                    pipelineRun,
                                    seller,
                                    resolveErrorMessage(exception),
                                    itemStartedAt,
                                    itemEndedAt
                            )
                    );

                    log.warn(
                            "일별 파이프라인 실행 실패. sellerId={}, metricDate={}, message={}",
                            seller.getId(),
                            metricDate,
                            exception.getMessage()
                    );
                }
            }

            pipelineRun.complete(
                    determineStatus(sellers.size(), processedSellerCount, failedSellerCount),
                    sellers.size(),
                    processedSellerCount,
                    failedSellerCount,
                    generatedInsightCount
            );

            PipelineRun savedPipelineRun = pipelineRunRepository.saveAndFlush(pipelineRun);

            pipelineMetricsRecorder.recordDailyPipelineRun(savedPipelineRun);

            return DailyPipelineRunResponse.from(savedPipelineRun, failedSellerIds);

        } finally {
            if (lockAcquired) {
                releaseExecutionLock(metricDate);
            }
        }
    }

    private void acquireExecutionLock(LocalDate metricDate) {
        try {
            pipelineExecutionLockRepository.saveAndFlush(
                    PipelineExecutionLock.create(PipelineType.DAILY, metricDate)
            );
        } catch (DataIntegrityViolationException exception) {
            recoverOrThrow(metricDate);
        }
    }

    private void recoverOrThrow(LocalDate metricDate) {
        PipelineExecutionLock existingLock = pipelineExecutionLockRepository.findByPipelineTypeAndMetricDate(
                        PipelineType.DAILY,
                        metricDate
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.PIPELINE_ALREADY_RUNNING));

        if (!isStaleLock(existingLock, metricDate)) {
            throw new BusinessException(ErrorCode.PIPELINE_ALREADY_RUNNING);
        }

        long deletedCount = pipelineExecutionLockRepository.deleteByPipelineTypeAndMetricDate(
                PipelineType.DAILY,
                metricDate
        );

        if (deletedCount == 0) {
            throw new BusinessException(ErrorCode.PIPELINE_ALREADY_RUNNING);
        }

        try {
            pipelineExecutionLockRepository.saveAndFlush(
                    PipelineExecutionLock.create(PipelineType.DAILY, metricDate)
            );
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.PIPELINE_ALREADY_RUNNING);
        }
    }

    private boolean isStaleLock(PipelineExecutionLock existingLock, LocalDate metricDate) {
        OffsetDateTime now = OffsetDateTime.now(ASIA_SEOUL);
        OffsetDateTime lockExpiresAt = existingLock.getCreatedAt().plusMinutes(lockTimeoutMinutes);

        if (lockExpiresAt.isAfter(now)) {
            return false;
        }

        PipelineRun latestRun = pipelineRunRepository.findFirstByPipelineTypeAndMetricDateOrderByIdDesc(
                PipelineType.DAILY,
                metricDate
        ).orElse(null);

        if (latestRun == null) {
            return true;
        }

        if (latestRun.getStatus() != PipelineRunStatus.RUNNING) {
            return true;
        }

        if (latestRun.getStartedAt() == null) {
            return true;
        }

        return latestRun.getStartedAt()
                .plusMinutes(lockTimeoutMinutes)
                .isBefore(now);
    }

    private void releaseExecutionLock(LocalDate metricDate) {
        pipelineExecutionLockRepository.deleteByPipelineTypeAndMetricDate(
                PipelineType.DAILY,
                metricDate
        );
    }

    private PipelineRunStatus determineStatus(
            int totalSellerCount,
            int processedSellerCount,
            int failedSellerCount
    ) {
        if (totalSellerCount == 0 || failedSellerCount == 0) {
            return PipelineRunStatus.SUCCESS;
        }

        if (processedSellerCount == 0) {
            return PipelineRunStatus.FAILED;
        }

        return PipelineRunStatus.PARTIAL_SUCCESS;
    }

    private String resolveErrorMessage(Exception exception) {
        if (exception instanceof BusinessException businessException) {
            return businessException.getMessage();
        }

        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }

        return ErrorCode.INTERNAL_SERVER_ERROR.getMessage();
    }
}
