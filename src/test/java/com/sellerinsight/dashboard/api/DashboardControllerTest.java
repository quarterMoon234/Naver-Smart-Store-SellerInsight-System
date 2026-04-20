package com.sellerinsight.dashboard.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sellerinsight.importjob.domain.ImportJobRepository;
import com.sellerinsight.insight.domain.Insight;
import com.sellerinsight.insight.domain.InsightRepository;
import com.sellerinsight.insight.domain.InsightSeverity;
import com.sellerinsight.insight.domain.InsightType;
import com.sellerinsight.insight.domain.Recommendation;
import com.sellerinsight.insight.domain.RecommendationRepository;
import com.sellerinsight.metric.domain.DailyMetric;
import com.sellerinsight.metric.domain.DailyMetricRepository;
import com.sellerinsight.order.domain.CustomerOrderRepository;
import com.sellerinsight.order.domain.OrderItemRepository;
import com.sellerinsight.pipeline.domain.PipelineRun;
import com.sellerinsight.pipeline.domain.PipelineRunItem;
import com.sellerinsight.pipeline.domain.PipelineRunItemRepository;
import com.sellerinsight.pipeline.domain.PipelineRunRepository;
import com.sellerinsight.pipeline.domain.PipelineRunStatus;
import com.sellerinsight.pipeline.domain.PipelineTriggerType;
import com.sellerinsight.pipeline.domain.PipelineType;
import com.sellerinsight.product.domain.ProductRepository;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerCredentialRepository;
import com.sellerinsight.seller.domain.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PipelineRunItemRepository pipelineRunItemRepository;

    @Autowired
    private PipelineRunRepository pipelineRunRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private InsightRepository insightRepository;

    @Autowired
    private DailyMetricRepository dailyMetricRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImportJobRepository importJobRepository;

    @Autowired
    private SellerCredentialRepository sellerCredentialRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @BeforeEach
    void setUp() {
        pipelineRunItemRepository.deleteAll();
        pipelineRunRepository.deleteAll();
        recommendationRepository.deleteAll();
        insightRepository.deleteAll();
        dailyMetricRepository.deleteAll();
        orderItemRepository.deleteAll();
        customerOrderRepository.deleteAll();
        productRepository.deleteAll();
        importJobRepository.deleteAll();
        sellerCredentialRepository.deleteAll();
        sellerRepository.deleteAll();
    }

    @Test
    void getDashboardAndDashboardSubResources() throws Exception {
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create("seller-001", "sellerinsight-store")
        );

        dailyMetricRepository.saveAndFlush(
                DailyMetric.create(
                        seller,
                        LocalDate.of(2026, 4, 14),
                        3,
                        new BigDecimal("30000"),
                        new BigDecimal("10000"),
                        0,
                        2
                )
        );

        dailyMetricRepository.saveAndFlush(
                DailyMetric.create(
                        seller,
                        LocalDate.of(2026, 4, 15),
                        4,
                        new BigDecimal("40000"),
                        new BigDecimal("10000"),
                        1,
                        1
                )
        );

        dailyMetricRepository.saveAndFlush(
                DailyMetric.create(
                        seller,
                        LocalDate.of(2026, 4, 16),
                        5,
                        new BigDecimal("50000"),
                        new BigDecimal("10000"),
                        2,
                        1
                )
        );

        Insight mediumInsight = insightRepository.saveAndFlush(
                Insight.create(
                        seller,
                        LocalDate.of(2026, 4, 15),
                        InsightType.SOLD_OUT_RISK,
                        InsightSeverity.MEDIUM,
                        "품절 상품이 존재합니다.",
                        "중간 위험도 인사이트",
                        "{\"soldOutProductCount\":1}",
                        "SoldOutRiskRule"
                )
        );

        recommendationRepository.saveAndFlush(
                Recommendation.create(
                        mediumInsight,
                        1,
                        "CHECK_STOCK",
                        "재고를 확인하세요.",
                        "품절 상품의 재고를 먼저 확인하세요.",
                        "SoldOutRiskRule"
                )
        );

        Insight highInsight = insightRepository.saveAndFlush(
                Insight.create(
                        seller,
                        LocalDate.of(2026, 4, 16),
                        InsightType.ORDER_DROP,
                        InsightSeverity.HIGH,
                        "전일 대비 주문 수가 감소했습니다.",
                        "고위험도 인사이트",
                        "{\"changeRate\":-50.0}",
                        "OrderDropRule"
                )
        );

        recommendationRepository.saveAndFlush(
                Recommendation.create(
                        highInsight,
                        1,
                        "CHECK_TOP_PRODUCTS",
                        "주문 감소 상품을 확인하세요.",
                        "주문 감소 원인을 우선 확인하세요.",
                        "OrderDropRule"
                )
        );

        Insight lowInsight = insightRepository.saveAndFlush(
                Insight.create(
                        seller,
                        LocalDate.of(2026, 4, 16),
                        InsightType.SOLD_OUT_RISK,
                        InsightSeverity.LOW,
                        "품절 상품이 일부 존재합니다.",
                        "저위험도 인사이트",
                        "{\"soldOutProductCount\":2}",
                        "SoldOutRiskRule"
                )
        );

        recommendationRepository.saveAndFlush(
                Recommendation.create(
                        lowInsight,
                        1,
                        "RESTOCK_PRIORITY",
                        "보충 우선순위를 확인하세요.",
                        "우선순위가 높은 상품부터 보충하세요.",
                        "SoldOutRiskRule"
                )
        );

        PipelineRun pipelineRun = PipelineRun.start(
                PipelineType.DAILY,
                PipelineTriggerType.SCHEDULED,
                LocalDate.of(2026, 4, 16)
        );
        pipelineRun.complete(PipelineRunStatus.SUCCESS, 1, 1, 0, 2);

        PipelineRun savedPipelineRun = pipelineRunRepository.saveAndFlush(pipelineRun);

        pipelineRunItemRepository.saveAndFlush(
                PipelineRunItem.success(savedPipelineRun, seller, 2)
        );

        mockMvc.perform(
                        get("/api/v1/sellers/{sellerId}/dashboard", seller.getId())
                                .param("metricDays", "3")
                                .param("insightLimit", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerId").value(seller.getId()))
                .andExpect(jsonPath("$.data.sellerName").value("sellerinsight-store"))
                .andExpect(jsonPath("$.data.latestMetricDate").value("2026-04-16"))
                .andExpect(jsonPath("$.data.latestMetric.orderCount").value(5))
                .andExpect(jsonPath("$.data.metricTrendDays").value(3))
                .andExpect(jsonPath("$.data.metricTrend.length()").value(3))
                .andExpect(jsonPath("$.data.insightSummary.totalCount").value(3))
                .andExpect(jsonPath("$.data.insightSummary.lowCount").value(1))
                .andExpect(jsonPath("$.data.insightSummary.mediumCount").value(1))
                .andExpect(jsonPath("$.data.insightSummary.highCount").value(1))
                .andExpect(jsonPath("$.data.recentInsightCount").value(2))
                .andExpect(jsonPath("$.data.recentInsights.length()").value(2))
                .andExpect(jsonPath("$.data.latestPipelineStatus.metricDate").value("2026-04-16"))
                .andExpect(jsonPath("$.data.latestPipelineStatus.triggerType").value("SCHEDULED"))
                .andExpect(jsonPath("$.data.latestPipelineStatus.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.latestPipelineStatus.generatedInsightCount").value(2));

        mockMvc.perform(
                        get("/api/v1/sellers/{sellerId}/dashboard/metrics", seller.getId())
                                .param("from", "2026-04-14")
                                .param("to", "2026-04-16")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.metricCount").value(3))
                .andExpect(jsonPath("$.data.metrics[0].metricDate").value("2026-04-14"))
                .andExpect(jsonPath("$.data.metrics[2].metricDate").value("2026-04-16"));

        mockMvc.perform(
                        get("/api/v1/sellers/{sellerId}/dashboard/insights/recent", seller.getId())
                                .param("limit", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.limit").value(2))
                .andExpect(jsonPath("$.data.insightCount").value(2))
                .andExpect(jsonPath("$.data.insights[0].metricDate").value("2026-04-16"))
                .andExpect(jsonPath("$.data.insights[1].metricDate").value("2026-04-16"));
    }
}
