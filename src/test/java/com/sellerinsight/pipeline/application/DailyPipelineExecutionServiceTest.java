package com.sellerinsight.pipeline.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sellerinsight.insight.api.dto.InsightsByDateResponse;
import com.sellerinsight.insight.application.InsightGenerationService;
import com.sellerinsight.metric.application.DailyMetricAggregationService;
import com.sellerinsight.pipeline.api.dto.DailyPipelineRunResponse;
import com.sellerinsight.pipeline.domain.PipelineRun;
import com.sellerinsight.pipeline.domain.PipelineRunItem;
import com.sellerinsight.pipeline.domain.PipelineRunItemRepository;
import com.sellerinsight.pipeline.domain.PipelineRunRepository;
import com.sellerinsight.pipeline.domain.PipelineRunStatus;
import com.sellerinsight.pipeline.domain.PipelineTriggerType;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerRepository;
import com.sellerinsight.seller.domain.SellerStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyPipelineExecutionServiceTest {

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

        when(dailyMetricAggregationService.aggregate(anyLong(), eq(metricDate)))
                .thenAnswer(invocation -> {
                    Long sellerId = invocation.getArgument(0);
                    if (sellerId.equals(2L)) {
                        throw new IllegalStateException("aggregation failed");
                    }
                    return null;
                });

        when(pipelineRunRepository.saveAndFlush(any(PipelineRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(pipelineRunItemRepository.save(any(PipelineRunItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DailyPipelineExecutionService service = new DailyPipelineExecutionService(
                sellerRepository,
                dailyMetricAggregationService,
                insightGenerationService,
                pipelineRunRepository,
                pipelineRunItemRepository
        );

        DailyPipelineRunResponse result = service.run(metricDate);

        assertThat(result.metricDate()).isEqualTo(metricDate);
        assertThat(result.totalSellerCount()).isEqualTo(2);
        assertThat(result.processedSellerCount()).isEqualTo(1);
        assertThat(result.failedSellerCount()).isEqualTo(1);
        assertThat(result.generatedInsightCount()).isEqualTo(2);
        assertThat(result.failedSellerIds()).containsExactly(2L);
        assertThat(result.triggerType()).isEqualTo(PipelineTriggerType.MANUAL);
        assertThat(result.status()).isEqualTo(PipelineRunStatus.PARTIAL_SUCCESS);

        verify(dailyMetricAggregationService).aggregate(1L, metricDate);
        verify(dailyMetricAggregationService).aggregate(2L, metricDate);
        verify(insightGenerationService).generate(1L, metricDate);
        verify(insightGenerationService, never()).generate(2L, metricDate);
        verify(pipelineRunRepository, times(2)).saveAndFlush(any(PipelineRun.class));
        verify(pipelineRunItemRepository, times(2)).save(any(PipelineRunItem.class));
    }
}
