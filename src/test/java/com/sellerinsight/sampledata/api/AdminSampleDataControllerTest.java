package com.sellerinsight.sampledata.api;

import com.sellerinsight.importjob.domain.ImportJobRepository;
import com.sellerinsight.insight.domain.InsightRepository;
import com.sellerinsight.insight.domain.RecommendationRepository;
import com.sellerinsight.metric.domain.DailyMetricRepository;
import com.sellerinsight.order.domain.CustomerOrderRepository;
import com.sellerinsight.order.domain.OrderItemRepository;
import com.sellerinsight.pipeline.domain.PipelineRunItemRepository;
import com.sellerinsight.pipeline.domain.PipelineRunRepository;
import com.sellerinsight.product.domain.ProductRepository;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerCredentialRepository;
import com.sellerinsight.seller.domain.SellerRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminSampleDataControllerTest {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

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
    void bootstrapSampleDataAndRunFullCycle() throws Exception {
        LocalDate previousMetricDate = LocalDate.now(ASIA_SEOUL).minusDays(2);
        LocalDate targetMetricDate = LocalDate.now(ASIA_SEOUL).minusDays(1);

        mockMvc.perform(
                        post("/api/v1/admin/sample-data/bootstrap")
                                .param("scenario", "default")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenario").value("default"))
                .andExpect(jsonPath("$.data.targetMetricDate").value(targetMetricDate.toString()))
                .andExpect(jsonPath("$.data.sellerCount").value(2))
                .andExpect(jsonPath("$.data.productCount").value(5))
                .andExpect(jsonPath("$.data.orderCount").value(14))
                .andExpect(jsonPath("$.data.orderItemCount").value(14))
                .andExpect(jsonPath("$.data.sellers.length()").value(2));

        mockMvc.perform(
                        post("/api/v1/admin/pipelines/daily")
                                .param("date", previousMetricDate.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.metricDate").value(previousMetricDate.toString()))
                .andExpect(jsonPath("$.data.totalSellerCount").value(2))
                .andExpect(jsonPath("$.data.processedSellerCount").value(2))
                .andExpect(jsonPath("$.data.failedSellerCount").value(0));

        mockMvc.perform(
                        post("/api/v1/admin/pipelines/daily")
                                .param("date", targetMetricDate.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.metricDate").value(targetMetricDate.toString()))
                .andExpect(jsonPath("$.data.totalSellerCount").value(2))
                .andExpect(jsonPath("$.data.processedSellerCount").value(2))
                .andExpect(jsonPath("$.data.failedSellerCount").value(0))
                .andExpect(jsonPath("$.data.generatedInsightCount").value(3))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.durationMs").isNumber());

        Seller alphaSeller = sellerRepository.findAll().stream()
                .filter(seller -> "sample-seller-alpha".equals(seller.getExternalSellerId()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(
                        get("/api/v1/sellers/{sellerId}/dashboard", alphaSeller.getId())
                                .param("metricDays", "7")
                                .param("insightLimit", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerId").value(alphaSeller.getId()))
                .andExpect(jsonPath("$.data.latestMetricDate").value(targetMetricDate.toString()))
                .andExpect(jsonPath("$.data.latestMetric.orderCount").value(2))
                .andExpect(jsonPath("$.data.latestMetric.soldOutProductCount").value(1))
                .andExpect(jsonPath("$.data.latestMetric.staleProductCount").value(1))
                .andExpect(jsonPath("$.data.insightSummary.totalCount").value(5))
                .andExpect(jsonPath("$.data.latestPipelineStatus.status").value("SUCCESS"));

        mockMvc.perform(
                        get("/api/v1/sellers/{sellerId}/dashboard/insights/recent", alphaSeller.getId())
                                .param("limit", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.insightCount").value(5));

        mockMvc.perform(
                        get("/api/v1/admin/pipelines/daily/runs")
                                .param("limit", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].metricDate").value(targetMetricDate.toString()))
                .andExpect(jsonPath("$.data[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].durationMs").isNumber());

        Long runId = pipelineRunRepository.findAllByPipelineTypeOrderByIdDesc(
                com.sellerinsight.pipeline.domain.PipelineType.DAILY
        ).get(0).getId();

        mockMvc.perform(
                        get("/api/v1/admin/pipelines/daily/runs/{runId}", runId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.run.durationMs").isNumber())
                .andExpect(jsonPath("$.data.itemCount").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].durationMs").isNumber());
    }

    @Test
    void bootstrapLargeSampleData() throws Exception {
        LocalDate targetMetricDate = LocalDate.now(ASIA_SEOUL).minusDays(1);

        mockMvc.perform(
                        post("/api/v1/admin/sample-data/bootstrap")
                                .param("scenario", "large")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenario").value("large"))
                .andExpect(jsonPath("$.data.targetMetricDate").value(targetMetricDate.toString()))
                .andExpect(jsonPath("$.data.sellerCount").value(30))
                .andExpect(jsonPath("$.data.productCount").value(600))
                .andExpect(jsonPath("$.data.orderCount").value(4800))
                .andExpect(jsonPath("$.data.orderItemCount").value(4800))
                .andExpect(jsonPath("$.data.sellers.length()").value(30));
    }
}
