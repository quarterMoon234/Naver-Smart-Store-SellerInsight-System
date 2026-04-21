package com.sellerinsight.insight.application;

import com.sellerinsight.insight.domain.InsightSeverity;
import com.sellerinsight.insight.domain.InsightType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class AverageOrderAmountDropRule implements InsightRule {

    @Override
    public Optional<InsightCandidate> evaluate(MetricContext context) {
        if (context.previousMetric() == null) {
            return Optional.empty();
        }

        BigDecimal previousAverageOrderAmount = context.previousMetric().getAverageOrderAmount();
        BigDecimal currentAverageOrderAmount = context.currentMetric().getAverageOrderAmount();

        if (previousAverageOrderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        double changeRate = currentAverageOrderAmount
                .subtract(previousAverageOrderAmount)
                .divide(previousAverageOrderAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        if (changeRate > -20.0) {
            return Optional.empty();
        }

        InsightSeverity severity = changeRate <= -35.0
                ? InsightSeverity.HIGH
                : InsightSeverity.MEDIUM;

        return Optional.of(new InsightCandidate(
                InsightType.AVERAGE_ORDER_AMOUNT_DROP,
                severity,
                "평균 주문 금액이 감소했습니다.",
                "전일 평균 주문 금액 "
                        + previousAverageOrderAmount
                        + "원에서 대상일 "
                        + currentAverageOrderAmount
                        + "원으로 감소했습니다.",
                Map.of(
                        "previousAverageOrderAmount", previousAverageOrderAmount,
                        "currentAverageOrderAmount", currentAverageOrderAmount,
                        "changeRate", changeRate
                ),
                List.of(
                        new RecommendationCandidate(
                                1,
                                "CHECK_LOW_PRICE_ORDERS",
                                "저가 상품 주문 비중을 확인하세요.",
                                "평균 주문 금액 하락이 특정 저가 상품 또는 할인 상품 집중 때문인지 확인하세요."
                        ),
                        new RecommendationCandidate(
                                2,
                                "REVIEW_BUNDLE_STRATEGY",
                                "묶음 구매 전략을 검토하세요.",
                                "객단가 회복을 위해 세트 구성, 무료배송 기준, 추가 구매 유도 상품을 점검하세요."
                        )
                ),
                "AverageOrderAmountDropRule"
        ));
    }
}
