package com.sellerinsight.seller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellerinsight.common.security.CredentialEncryptor;
import com.sellerinsight.importjob.domain.ImportJobRepository;
import com.sellerinsight.insight.domain.InsightRepository;
import com.sellerinsight.insight.domain.RecommendationRepository;
import com.sellerinsight.metric.domain.DailyMetricRepository;
import com.sellerinsight.order.domain.CustomerOrderRepository;
import com.sellerinsight.order.domain.OrderItemRepository;
import com.sellerinsight.pipeline.domain.PipelineRunItemRepository;
import com.sellerinsight.pipeline.domain.PipelineRunRepository;
import com.sellerinsight.product.domain.ProductRepository;
import com.sellerinsight.seller.api.dto.UpsertSellerCredentialRequest;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerCredential;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SellerCredentialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private SellerCredentialRepository sellerCredentialRepository;

    @Autowired
    private CredentialEncryptor credentialEncryptor;

    @Autowired
    private ImportJobRepository importJobRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

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
    void upsertSellerCredential() throws Exception {
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create("naver-seller-001", "sellerinsight-store")
        );

        UpsertSellerCredentialRequest request = new UpsertSellerCredentialRequest(
                "naver-client-id",
                "naver-secret-1234"
        );

        mockMvc.perform(put("/api/v1/sellers/{sellerId}/credentials", seller.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerId").value(seller.getId()))
                .andExpect(jsonPath("$.data.clientId").value("naver-client-id"))
                .andExpect(jsonPath("$.data.clientSecretMasked").value("****1234"));

        SellerCredential sellerCredential = sellerCredentialRepository.findBySellerId(seller.getId())
                .orElseThrow();

        assertThat(sellerCredential.getEncryptedClientSecret()).isNotEqualTo("naver-secret-1234");
        assertThat(credentialEncryptor.decrypt(sellerCredential.getEncryptedClientSecret()))
                .isEqualTo("naver-secret-1234");
    }

    @Test
    void getSellerCredential() throws Exception {
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create("naver-seller-001", "sellerinsight-store")
        );

        SellerCredential sellerCredential = SellerCredential.create(
                seller,
                "naver-client-id",
                credentialEncryptor.encrypt("naver-secret-1234"),
                "1234"
        );

        sellerCredentialRepository.saveAndFlush(sellerCredential);

        mockMvc.perform(get("/api/v1/sellers/{sellerId}/credentials", seller.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerId").value(seller.getId()))
                .andExpect(jsonPath("$.data.clientId").value("naver-client-id"))
                .andExpect(jsonPath("$.data.clientSecretMasked").value("****1234"));
    }
}
