package com.sellerinsight.pipeline.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sellerinsight.common.error.BusinessException;
import com.sellerinsight.common.error.ErrorCode;
import com.sellerinsight.insight.api.dto.InsightsByDateResponse;
import com.sellerinsight.insight.application.InsightGenerationService;
import com.sellerinsight.metric.application.DailyMetricAggregationService;
import com.sellerinsight.pipeline.api.dto.DailyPipelineRunResponse;
import com.sellerinsight.pipeline.domain.PipelineExecutionLock;
import com.sellerinsight.pipeline.domain.PipelineExecutionLockRepository;
import com.sellerinsight.pipeline.domain.PipelineRun;
import com.sellerinsight.pipeline.domain.PipelineRunItem;
import com.sellerinsight.pipeline.domain.PipelineRunItemRepository;
import com.sellerinsight.pipeline.domain.PipelineRunRepository;
import com.sellerinsight.pipeline.domain.PipelineRunStatus;
import com.sellerinsight.pipeline.domain.PipelineTriggerType;
import com.sellerinsight.pipeline.domain.PipelineType;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerRepository;
import com.sellerinsight.seller.domain.SellerStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DailyPipelineExecutionServiceTest {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private DailyMetricAggregationService dailyMetricAggregationService;

    @Mock
    private InsightGenerationService insightGenerationService;

    @Mock
    private PipelineRunRepository pipelineRunRepository;

    @Mock
    private PipelineRunItemRepository pipelineRunItemRepository;

    @Mock
    private PipelineExecutionLockRepository pipelineExecutionLockRepository;

    @Test
    void runProcessesConnectedSellersAndContinuesOnFailure() {
        Seller seller1 = mock(Seller.class);
        Seller seller2 = mock(Seller.class);

        when(seller1.getId()).thenReturn(1L);
        when(seller2.getId()).thenReturn(2L);

        LocalDate metricDate = LocalDate.of(2026, 4, 19);

        when(sellerRepository.findAllByStatus(SellerStatus.CONNECTED))
                .thenReturn(List.of(seller1, seller2));

        when(insightGenerationService.generate(eq(1L), eq(metricDate)))
                .thenReturn(new InsightsByDateResponse(1L, metricDate, 2, List.of()));

        doAnswer(invocation -> {
            Long sellerId = invocation.getArgument(0);
            if (Long.valueOf(2L).equals(sellerId)) {
                throw new IllegalStateException("aggregation failed");
            }
            return null;
        }).when(dailyMetricAggregationService).aggregate(any(), eq(metricDate));

        when(pipelineExecutionLockRepository.saveAndFlush(any(PipelineExecutionLock.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(pipelineRunRepository.saveAndFlush(any(PipelineRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(pipelineRunItemRepository.save(any(PipelineRunItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DailyPipelineExecutionService service = createServiceWithTimeout(30);

        DailyPipelineRunResponse result = service.run(metricDate);

        assertThat(result.metricDate()).isEqualTo(metricDate);
        assertThat(result.totalSellerCount()).isEqualTo(2);
        assertThat(result.processedSellerCount()).isEqualTo(1);
        assertThat(result.failedSellerCount()).isEqualTo(1);
        assertThat(result.generatedInsightCount()).isEqualTo(2);
        assertThat(result.failedSellerIds()).containsExactly(2L);
        assertThat(result.triggerType()).isEqualTo(PipelineTriggerType.MANUAL);
        assertThat(result.status()).isEqualTo(PipelineRunStatus.PARTIAL_SUCCESS);

        verify(pipelineExecutionLockRepository).saveAndFlush(any(PipelineExecutionLock.class));
        verify(pipelineExecutionLockRepository).deleteByPipelineTypeAndMetricDate(
                PipelineType.DAILY,
                metricDate
        );
        verify(dailyMetricAggregationService).aggregate(1L, metricDate);
        verify(dailyMetricAggregationService).aggregate(2L, metricDate);
        verify(insightGenerationService).generate(1L, metricDate);
        verify(insightGenerationService, never()).generate(2L, metricDate);
        verify(pipelineRunRepository, times(2)).saveAndFlush(any(PipelineRun.class));
        verify(pipelineRunItemRepository, times(2)).save(any(PipelineRunItem.class));
    }

    @Test
    void runThrowsConflictWhenCurrentLockIsStillValid() {
        LocalDate metricDate = LocalDate.of(2026, 4, 19);

        PipelineExecutionLock lock = mock(PipelineExecutionLock.class);
        when(lock.getCreatedAt()).thenReturn(OffsetDateTime.now(ASIA_SEOUL).minusMinutes(5));

        when(pipelineExecutionLockRepository.saveAndFlush(any(PipelineExecutionLock.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        when(pipelineExecutionLockRepository.findByPipelineTypeAndMetricDate(PipelineType.DAILY, metricDate))
                .thenReturn(java.util.Optional.of(lock));

        DailyPipelineExecutionService service = createServiceWithTimeout(30);

        assertThatThrownBy(() -> service.run(metricDate))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PIPELINE_ALREADY_RUNNING.getMessage());

        verify(pipelineExecutionLockRepository, never())
                .deleteByPipelineTypeAndMetricDate(any(), any());
        verifyNoInteractions(
                sellerRepository,
                dailyMetricAggregationService,
                insightGenerationService,
                pipelineRunRepository,
                pipelineRunItemRepository
        );
    }

    @Test
    void runRecoversStaleLockAndProceeds() {
        LocalDate metricDate = LocalDate.of(2026, 4, 19);

        PipelineExecutionLock lock = mock(PipelineExecutionLock.class);
        when(lock.getCreatedAt()).thenReturn(OffsetDateTime.now(ASIA_SEOUL).minusMinutes(40));

        PipelineRun latestRun = mock(PipelineRun.class);
        when(latestRun.getStatus()).thenReturn(PipelineRunStatus.FAILED);

        when(pipelineExecutionLockRepository.saveAndFlush(any(PipelineExecutionLock.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(pipelineExecutionLockRepository.findByPipelineTypeAndMetricDate(PipelineType.DAILY, metricDate))
                .thenReturn(java.util.Optional.of(lock));

        when(pipelineExecutionLockRepository.deleteByPipelineTypeAndMetricDate(PipelineType.DAILY, metricDate))
                .thenReturn(1L);

        when(pipelineRunRepository.findFirstByPipelineTypeAndMetricDateOrderByIdDesc(PipelineType.DAILY, metricDate))
                .thenReturn(java.util.Optional.of(latestRun));

        when(sellerRepository.findAllByStatus(SellerStatus.CONNECTED))
                .thenReturn(List.of());

        when(pipelineRunRepository.saveAndFlush(any(PipelineRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DailyPipelineExecutionService service = createServiceWithTimeout(30);

        DailyPipelineRunResponse result = service.run(metricDate);

        assertThat(result.metricDate()).isEqualTo(metricDate);
        assertThat(result.totalSellerCount()).isEqualTo(0);
        assertThat(result.status()).isEqualTo(PipelineRunStatus.SUCCESS);

        verify(pipelineExecutionLockRepository, times(2))
                .saveAndFlush(any(PipelineExecutionLock.class));
        verify(pipelineExecutionLockRepository, times(2))
                .deleteByPipelineTypeAndMetricDate(PipelineType.DAILY, metricDate);
        verify(pipelineRunRepository)
                .findFirstByPipelineTypeAndMetricDateOrderByIdDesc(PipelineType.DAILY, metricDate);
    }

    private DailyPipelineExecutionService createServiceWithTimeout(long timeoutMinutes) {
        DailyPipelineExecutionService service = new DailyPipelineExecutionService(
                sellerRepository,
                dailyMetricAggregationService,
                insightGenerationService,
                pipelineRunRepository,
                pipelineRunItemRepository,
                pipelineExecutionLockRepository
        );

        ReflectionTestUtils.setField(service, "lockTimeoutMinutes", timeoutMinutes);

        return service;
    }
}
