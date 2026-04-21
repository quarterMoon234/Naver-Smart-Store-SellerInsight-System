package com.sellerinsight.insight.application;

import com.sellerinsight.insight.domain.InsightSeverity;
import com.sellerinsight.insight.domain.InsightType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class StaleProductRiskRule implements InsightRule {

    @Override
    public Optional<InsightCandidate> evaluate(MetricContext context) {
        int staleProductCount = context.currentMetric().getStaleProductCount();

        if (staleProductCount <= 0) {
            return Optional.empty();
        }

        InsightSeverity severity = staleProductCount >= 5
                ? InsightSeverity.HIGH
                : InsightSeverity.MEDIUM;

        return Optional.of(new InsightCandidate(
                InsightType.STALE_PRODUCT_RISK,
                severity,
                "장기 미판매 상품이 증가하고 있습니다.",
                "최근 판매 이력이 없는 상품이 " + staleProductCount + "개 확인되었습니다.",
                Map.of(
                        "staleProductCount", staleProductCount
                ),
                List.of(
                        new RecommendationCandidate(
                                1,
                                "REVIEW_STALE_PRODUCTS",
                                "장기 미판매 상품을 재점검하세요.",
                                "최근 판매가 없는 상품의 가격, 대표 이미지, 상세페이지, 키워드를 우선 점검하세요."
                        ),
                        new RecommendationCandidate(
                                2,
                                "PROMOTION_OR_DELIST",
                                "프로모션 또는 정리를 검토하세요.",
                                "회전율이 낮은 상품은 할인, 묶음 구성, 노출 축소 또는 판매 종료를 검토하세요."
                        )
                ),
                "StaleProductRiskRule"
        ));
    }
}
