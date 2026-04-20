package com.sellerinsight.insight.application;

import com.sellerinsight.insight.domain.InsightSeverity;
import com.sellerinsight.insight.domain.InsightType;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Order(2)
public class SoldOutRiskRule implements InsightRule {

    @Override
    public Optional<InsightCandidate> evaluate(MetricContext context) {
        int soldOutProductCount = context.currentMetric().getSoldOutProductCount();

        if (soldOutProductCount <= 0) {
            return Optional.empty();
        }

        return Optional.of(new InsightCandidate(
                InsightType.SOLD_OUT_RISK,
                soldOutProductCount >= 3 ? InsightSeverity.HIGH : InsightSeverity.MEDIUM,
                "품절 상품이 존재합니다.",
                "현재 품절 상태의 상품이 " + soldOutProductCount + "개 확인되었습니다.",
                Map.of(
                        "soldOutProductCount", soldOutProductCount
                ),
                List.of(
                        new RecommendationCandidate(
                                1,
                                "RESTOCK_PRIORITY",
                                "품절 상품 보충 우선순위를 정하세요.",
                                "판매 가능성이 높은 상품부터 재고를 우선 보충하세요."
                        ),
                        new RecommendationCandidate(
                                2,
                                "CHECK_EXPOSURE_LOSS",
                                "노출 손실 여부를 확인하세요.",
                                "품절이 오래 지속된 상품은 검색/노출 손실이 발생할 수 있습니다."
                        )
                ),
                "SoldOutRiskRule"
        ));
    }
}
