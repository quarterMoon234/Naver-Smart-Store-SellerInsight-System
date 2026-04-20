package com.sellerinsight.insight.application;

import com.sellerinsight.insight.domain.InsightSeverity;
import com.sellerinsight.insight.domain.InsightType;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Order(1)
public class OrderDropRule implements InsightRule {

    @Override
    public Optional<InsightCandidate> evaluate(MetricContext context) {
        if (context.previousMetric() == null) {
            return Optional.empty();
        }

        int previousOrderCount = context.previousMetric().getOrderCount();
        int currentOrderCount = context.currentMetric().getOrderCount();

        if (previousOrderCount <= 0) {
            return Optional.empty();
        }

        double changeRate = ((double) currentOrderCount - previousOrderCount) / previousOrderCount * 100.0;

        if (changeRate > -20.0) {
            return Optional.empty();
        }

        return Optional.of(new InsightCandidate(
                InsightType.ORDER_DROP,
                changeRate <= -30.0 ? InsightSeverity.HIGH : InsightSeverity.MEDIUM,
                "전일 대비 주문 수가 감소했습니다.",
                "전일 주문 " + previousOrderCount + "건에서 오늘 " + currentOrderCount + "건으로 감소했습니다.",
                Map.of(
                        "previousOrderCount", previousOrderCount,
                        "currentOrderCount", currentOrderCount,
                        "changeRate", changeRate
                ),
                List.of(
                        new RecommendationCandidate(
                                1,
                                "CHECK_TOP_PRODUCTS",
                                "주문 감소 상품을 우선 확인하세요.",
                                "주문 감소폭이 큰 상품의 재고, 가격, 상세페이지 변경 여부를 먼저 점검하세요."
                        ),
                        new RecommendationCandidate(
                                2,
                                "CHECK_STOCK_STATUS",
                                "재고와 품절 상태를 확인하세요.",
                                "판매중 상품이 실제로 주문 가능한 상태인지 확인해보세요."
                        )
                ),
                "OrderDropRule"
        ));
    }
}
