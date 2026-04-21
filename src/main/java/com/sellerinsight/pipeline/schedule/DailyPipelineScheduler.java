package com.sellerinsight.pipeline.schedule;

import com.sellerinsight.common.error.BusinessException;
import com.sellerinsight.common.error.ErrorCode;
import com.sellerinsight.pipeline.api.dto.DailyPipelineRunResponse;
import com.sellerinsight.pipeline.application.DailyPipelineExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.pipeline.daily", name = "enabled", havingValue = "true")
public class DailyPipelineScheduler {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

    private final DailyPipelineExecutionService dailyPipelineExecutionService;

    @Scheduled(cron = "${app.pipeline.daily.cron}", zone = "Asia/Seoul")
    public void run() {
        LocalDate targetDate = LocalDate.now(ASIA_SEOUL).minusDays(1);

        try {
            DailyPipelineRunResponse result = dailyPipelineExecutionService.runScheduled(targetDate);

            log.info(
                    "일별 파이프라인 실행 완료. runId={}, metricDate={}, status={}, totalSellerCount={}, processedSellerCount={}, failedSellerCount={}, generatedInsightCount={}",
                    result.runId(),
                    result.metricDate(),
                    result.status(),
                    result.totalSellerCount(),
                    result.processedSellerCount(),
                    result.failedSellerCount(),
                    result.generatedInsightCount()
            );
        } catch (BusinessException exception) {
            if (exception.getErrorCode() == ErrorCode.PIPELINE_ALREADY_RUNNING) {
                log.info(
                        "일별 파이프라인 실행 건너뜀. metricDate={}, reason={}",
                        targetDate,
                        exception.getMessage()
                );
                return;
            }

            throw exception;
        }
    }
}
