package com.sellerinsight.dashboard.application;

import com.sellerinsight.common.error.BusinessException;
import com.sellerinsight.common.error.ErrorCode;
import com.sellerinsight.dashboard.api.dto.DashboardInsightSummaryResponse;
import com.sellerinsight.dashboard.api.dto.DashboardMetricPointResponse;
import com.sellerinsight.dashboard.api.dto.DashboardMetricRangeResponse;
import com.sellerinsight.dashboard.api.dto.DashboardResponse;
import com.sellerinsight.dashboard.api.dto.RecentInsightsResponse;
import com.sellerinsight.dashboard.api.dto.SellerPipelineStatusResponse;
import com.sellerinsight.insight.api.dto.InsightResponse;
import com.sellerinsight.insight.api.dto.RecommendationResponse;
import com.sellerinsight.insight.domain.Insight;
import com.sellerinsight.insight.domain.InsightRepository;
import com.sellerinsight.insight.domain.InsightSeverity;
import com.sellerinsight.insight.domain.RecommendationRepository;
import com.sellerinsight.metric.api.dto.DailyMetricResponse;
import com.sellerinsight.metric.domain.DailyMetric;
import com.sellerinsight.metric.domain.DailyMetricRepository;
import com.sellerinsight.pipeline.domain.PipelineRunItemRepository;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryService {

    private final SellerRepository sellerRepository;
    private final DailyMetricRepository dailyMetricRepository;
    private final InsightRepository insightRepository;
    private final RecommendationRepository recommendationRepository;
    private final PipelineRunItemRepository pipelineRunItemRepository;

    public DashboardResponse getDashboard(
            Long sellerId,
            int metricDays,
            int insightLimit
    ) {
        Seller seller = getSeller(sellerId);

        int safeMetricDays = Math.max(metricDays, 1);
        int safeInsightLimit = Math.max(insightLimit, 1);

        Optional<DailyMetric> latestMetricOptional = dailyMetricRepository
                .findFirstBySellerIdOrderByMetricDateDesc(sellerId);

        List<DashboardMetricPointResponse> metricTrend = latestMetricOptional
                .map(latestMetric -> getMetricPoints(
                        sellerId,
                        latestMetric.getMetricDate().minusDays(safeMetricDays - 1L),
                        latestMetric.getMetricDate()
                ))
                .orElse(List.of());

        List<InsightResponse> recentInsights = getRecentInsightsInternal(sellerId, safeInsightLimit);

        DashboardInsightSummaryResponse insightSummary = DashboardInsightSummaryResponse.of(
                insightRepository.countBySellerIdAndSeverity(sellerId, InsightSeverity.LOW),
                insightRepository.countBySellerIdAndSeverity(sellerId, InsightSeverity.MEDIUM),
                insightRepository.countBySellerIdAndSeverity(sellerId, InsightSeverity.HIGH)
        );

        SellerPipelineStatusResponse latestPipelineStatus = pipelineRunItemRepository
                .findFirstBySellerIdOrderByIdDesc(sellerId)
                .map(SellerPipelineStatusResponse::from)
                .orElse(null);

        return new DashboardResponse(
                seller.getId(),
                seller.getSellerName(),
                latestMetricOptional.map(DailyMetric::getMetricDate).orElse(null),
                latestMetricOptional.map(DailyMetricResponse::from).orElse(null),
                safeMetricDays,
                metricTrend,
                insightSummary,
                recentInsights.size(),
                recentInsights,
                latestPipelineStatus
        );
    }

    public DashboardMetricRangeResponse getMetricRange(
            Long sellerId,
            LocalDate from,
            LocalDate to
    ) {
        getSeller(sellerId);

        if (from.isAfter(to)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<DashboardMetricPointResponse> metrics = getMetricPoints(sellerId, from, to);

        return new DashboardMetricRangeResponse(
                sellerId,
                from,
                to,
                metrics.size(),
                metrics
        );
    }

    public RecentInsightsResponse getRecentInsights(Long sellerId, int limit) {
        getSeller(sellerId);

        int safeLimit = Math.max(limit, 1);
        List<InsightResponse> insights = getRecentInsightsInternal(sellerId, safeLimit);

        return new RecentInsightsResponse(
                sellerId,
                safeLimit,
                insights.size(),
                insights
        );
    }

    private Seller getSeller(Long sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private List<DashboardMetricPointResponse> getMetricPoints(
            Long sellerId,
            LocalDate from,
            LocalDate to
    ) {
        return dailyMetricRepository.findAllBySellerIdAndMetricDateBetweenOrderByMetricDateAsc(
                        sellerId,
                        from,
                        to
                ).stream()
                .map(DashboardMetricPointResponse::from)
                .toList();
    }

    private List<InsightResponse> getRecentInsightsInternal(Long sellerId, int limit) {
        PageRequest pageRequest = PageRequest.of(
                0,
                limit,
                Sort.by(
                        Sort.Order.desc("metricDate"),
                        Sort.Order.desc("id")
                )
        );

        return insightRepository.findAllBySellerId(sellerId, pageRequest).stream()
                .map(this::toInsightResponse)
                .toList();
    }

    private InsightResponse toInsightResponse(Insight insight) {
        List<RecommendationResponse> recommendations = recommendationRepository
                .findAllByInsightIdOrderByPriorityAsc(insight.getId()).stream()
                .map(RecommendationResponse::from)
                .toList();

        return InsightResponse.from(insight, recommendations);
    }
}
