package com.sellerinsight.insight.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellerinsight.common.error.BusinessException;
import com.sellerinsight.common.error.ErrorCode;
import com.sellerinsight.insight.api.dto.InsightResponse;
import com.sellerinsight.insight.api.dto.InsightsByDateResponse;
import com.sellerinsight.insight.api.dto.RecommendationResponse;
import com.sellerinsight.insight.domain.Insight;
import com.sellerinsight.insight.domain.InsightRepository;
import com.sellerinsight.insight.domain.Recommendation;
import com.sellerinsight.insight.domain.RecommendationRepository;
import com.sellerinsight.metric.domain.DailyMetric;
import com.sellerinsight.metric.domain.DailyMetricRepository;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InsightGenerationService {

    private final SellerRepository sellerRepository;
    private final DailyMetricRepository dailyMetricRepository;
    private final InsightRepository insightRepository;
    private final RecommendationRepository recommendationRepository;
    private final List<InsightRule> insightRules;
    private final ObjectMapper objectMapper;

    @Transactional
    public InsightsByDateResponse generate(Long sellerId, LocalDate metricDate) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        DailyMetric currentMetric = dailyMetricRepository.findBySellerIdAndMetricDate(
                        sellerId,
                        metricDate)
                .orElseThrow(() -> new BusinessException(ErrorCode.DAILY_METRIC_NOT_FOUND));

        DailyMetric previousMetric = dailyMetricRepository.findBySellerIdAndMetricDate(
                sellerId,
                metricDate.minusDays(1)
        ).orElse(null);

        MetricContext context = new MetricContext(
                sellerId,
                metricDate,
                currentMetric,
                previousMetric
        );

        for (InsightRule rule : insightRules) {
            rule.evaluate(context).ifPresent(candidate -> upsertInsight(seller, metricDate, candidate));
        }

        return getInsightsByDate(sellerId, metricDate);
    }

    public InsightsByDateResponse getInsightsByDate(Long sellerId, LocalDate metricDate) {
        List<InsightResponse> insights = insightRepository.findAllBySellerIdAndMetricDateOrderByIdAsc(sellerId, metricDate)
                .stream()
                .map(this::toInsightResponse)
                .toList();

        return new InsightsByDateResponse(
                sellerId,
                metricDate,
                insights.size(),
                insights
        );
    }

    public InsightResponse getInsight(Long sellerId, Long insightId) {
        Insight insight = insightRepository.findByIdAndSellerId(insightId, sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSIGHT_NOT_FOUND));

        return toInsightResponse(insight);
    }

    private void upsertInsight(Seller seller, LocalDate metricDate, InsightCandidate candidate) {
        String evidenceJson = toJson(candidate.evidence());

        Insight insight = insightRepository.findBySellerIdAndMetricDateAndInsightType(
                        seller.getId(),
                        metricDate,
                        candidate.insightType()
                )
                .map(existingInsight -> {
                    existingInsight.update(
                            candidate.severity(),
                            candidate.title(),
                            candidate.summary(),
                            evidenceJson,
                            candidate.generatedBy()
                    );
                    return existingInsight;
                })
                .orElseGet(() -> Insight.create(
                        seller,
                        metricDate,
                        candidate.insightType(),
                        candidate.severity(),
                        candidate.title(),
                        candidate.summary(),
                        evidenceJson,
                        candidate.generatedBy()
                ));

        Insight savedInsight = insightRepository.saveAndFlush(insight);

        recommendationRepository.deleteAllByInsightId(savedInsight.getId());

        List<Recommendation> recommendations = candidate.recommendations().stream()
                .map(recommendationCandidate -> Recommendation.create(
                        savedInsight,
                        recommendationCandidate.priority(),
                        recommendationCandidate.actionCode(),
                        recommendationCandidate.actionTitle(),
                        recommendationCandidate.actionMessage(),
                        candidate.generatedBy()
                ))
                .toList();

        recommendationRepository.saveAll(recommendations);
    }

    private InsightResponse toInsightResponse(Insight insight) {
        List<RecommendationResponse> recommendations = recommendationRepository.findAllByInsightIdOrderByPriorityAsc(
                        insight.getId()
                ).stream()
                .map(RecommendationResponse::from)
                .toList();

        return InsightResponse.from(insight, recommendations);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Insight evidence 직렬화에 실패했습니다.", exception);
        }
    }
}
