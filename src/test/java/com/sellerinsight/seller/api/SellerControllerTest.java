package com.sellerinsight.seller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellerinsight.importjob.domain.ImportJobRepository;
import com.sellerinsight.insight.domain.InsightRepository;
import com.sellerinsight.insight.domain.RecommendationRepository;
import com.sellerinsight.metric.domain.DailyMetricRepository;
import com.sellerinsight.order.domain.CustomerOrderRepository;
import com.sellerinsight.order.domain.OrderItemRepository;
import com.sellerinsight.pipeline.domain.PipelineRunItemRepository;
import com.sellerinsight.pipeline.domain.PipelineRunRepository;
import com.sellerinsight.product.domain.ProductRepository;
import com.sellerinsight.seller.api.dto.CreateSellerRequest;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerCredentialRepository;
import com.sellerinsight.seller.domain.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ImportJobRepository importJobRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private SellerCredentialRepository sellerCredentialRepository;

    @Autowired
    private DailyMetricRepository dailyMetricRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private InsightRepository insightRepository;

    @Autowired
    private PipelineRunItemRepository pipelineRunItemRepository;

    @Autowired
    private PipelineRunRepository pipelineRunRepository;

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
    void createSeller() throws Exception {
        CreateSellerRequest request = new CreateSellerRequest(
                "naver-seller-001",
                "sellerinsight-store"
        );

        mockMvc.perform(post("/api/v1/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.externalSellerId").value("naver-seller-001"))
                .andExpect(jsonPath("$.data.sellerName").value("sellerinsight-store"))
                .andExpect(jsonPath("$.data.status").value("CONNECTED"));
    }

    @Test
    void getSeller() throws Exception {
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create("naver-seller-001", "sellerinsight-store")
        );

        mockMvc.perform(get("/api/v1/sellers/{sellerId}", seller.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(seller.getId()))
                .andExpect(jsonPath("$.data.externalSellerId").value("naver-seller-001"))
                .andExpect(jsonPath("$.data.status").value("CONNECTED"));
    }
}
