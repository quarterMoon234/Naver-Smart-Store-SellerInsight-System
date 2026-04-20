package com.sellerinsight.pipeline.application;

import com.sellerinsight.insight.api.dto.InsightsByDateResponse;
import com.sellerinsight.insight.application.InsightGenerationService;
import com.sellerinsight.metric.application.DailyMetricAggregationService;
import com.sellerinsight.pipeline.api.dto.DailyPipelineRunResponse;
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

    public DailyPipelineRunResponse run(LocalDate metricDate) {
        List<Seller> sellers = sellerRepository.findAllByStatus(SellerStatus.CONNECTED);

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
            } catch (Exception exception) {
                failedSellerCount++;
                failedSellerIds.add(seller.getId());

                log.warn(
                        "일별 파이프라인 실행 실패. sellerId={}, metricDate={}, message={}",
                        seller.getId(),
                        metricDate,
                        exception.getMessage()
                );
            }
        }

        return new DailyPipelineRunResponse(
                metricDate,
                sellers.size(),
                processedSellerCount,
                failedSellerCount,
                generatedInsightCount,
                failedSellerIds
        );
    }
}
