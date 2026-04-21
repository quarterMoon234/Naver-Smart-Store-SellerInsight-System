package com.sellerinsight.insight.application;

import com.sellerinsight.insight.domain.InsightSeverity;
import com.sellerinsight.insight.domain.InsightType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class NoOrderRule implements InsightRule {

    @Override
    public Optional<InsightCandidate> evaluate(MetricContext context) {
        int currentOrderCount = context.currentMetric().getOrderCount();

        if (currentOrderCount > 0) {
            return Optional.empty();
        }

        Integer previousOrderCount = context.previousMetric() == null
                ? null
                : context.previousMetric().getOrderCount();

        InsightSeverity severity = previousOrderCount != null && previousOrderCount >= 3
                ? InsightSeverity.HIGH
                : InsightSeverity.MEDIUM;

        return Optional.of(new InsightCandidate(
                InsightType.NO_ORDER,
                severity,
                "오늘 주문이 발생하지 않았습니다.",
                "대상일에 주문이 0건으로 확인되었습니다. 노출, 가격, 재고, 판매 상태를 우선 점검해야 합니다.",
                Map.of(
                        "currentOrderCount", currentOrderCount,
                        "previousOrderCount", previousOrderCount == null ? "N/A" : previousOrderCount
                ),
                List.of(
                        new RecommendationCandidate(
                                1,
                                "CHECK_PRODUCT_VISIBILITY",
                                "상품 노출 상태를 확인하세요.",
                                "주요 상품이 검색과 카테고리에서 정상 노출되는지 먼저 확인하세요."
                        ),
                        new RecommendationCandidate(
                                2,
                                "CHECK_PURCHASE_BLOCKERS",
                                "구매 방해 요인을 점검하세요.",
                                "품절, 배송 정책, 가격 변경, 판매중지 상태 등 주문을 막는 요인이 있는지 확인하세요."
                        )
                ),
                "NoOrderRule"
        ));
    }
}
