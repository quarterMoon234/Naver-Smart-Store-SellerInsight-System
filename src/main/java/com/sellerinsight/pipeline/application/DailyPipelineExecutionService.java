package com.sellerinsight.pipeline.application;

import com.sellerinsight.common.error.BusinessException;
import com.sellerinsight.common.error.ErrorCode;
import com.sellerinsight.insight.api.dto.InsightsByDateResponse;
import com.sellerinsight.insight.application.InsightGenerationService;
import com.sellerinsight.metric.application.DailyMetricAggregationService;
import com.sellerinsight.pipeline.api.dto.DailyPipelineRunResponse;
import com.sellerinsight.pipeline.api.dto.PipelineRunDetailResponse;
import com.sellerinsight.pipeline.api.dto.PipelineRunItemResponse;
import com.sellerinsight.pipeline.api.dto.PipelineRunSummaryResponse;
import com.sellerinsight.pipeline.domain.*;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerRepository;
import com.sellerinsight.seller.domain.SellerStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyPipelineExecutionService {

    private final SellerRepository sellerRepository;
    private final DailyMetricAggregationService dailyMetricAggregationService;
    private final InsightGenerationService insightGenerationService;
    private final PipelineRunRepository pipelineRunRepository;
    private final PipelineRunItemRepository pipelineRunItemRepository;

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

    public DailyPipelineRunResponse run(LocalDate metricDate, PipelineTriggerType triggerType) {
        List<Seller> sellers = sellerRepository.findAllByStatus(SellerStatus.CONNECTED);

        PipelineRun pipelineRun = pipelineRunRepository.saveAndFlush(
                PipelineRun.start(PipelineType.DAILY, triggerType, metricDate)
        );

        int processedSellerCount = 0;
        int failedSellerCount = 0;
        int generatedInsightCount = 0;
        List<Long> failedSellerIds = new ArrayList<>();

        for (Seller seller : sellers) {
            try {
                dailyMetricAggregationService.aggregate(seller.getId(), metricDate);
                InsightsByDateResponse insightResult = insightGenerationService.generate(
                        seller.getId(),
                        metricDate
                );

                processedSellerCount++;
                generatedInsightCount += insightResult.insightCount();

                pipelineRunItemRepository.save(
                        PipelineRunItem.success(
                                pipelineRun,
                                seller,
                                insightResult.insightCount()
                        )
                );

            } catch (Exception exception) {
                failedSellerCount++;
                failedSellerIds.add(seller.getId());

                pipelineRunItemRepository.save(
                        PipelineRunItem.failed(
                                pipelineRun,
                                seller,
                                resolveErrorMessage(exception)
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

        return DailyPipelineRunResponse.from(savedPipelineRun, failedSellerIds);
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
