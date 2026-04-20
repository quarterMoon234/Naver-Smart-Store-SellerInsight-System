package com.sellerinsight.importjob.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sellerinsight.importjob.domain.ImportJob;
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
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void importOrdersCsv() throws Exception {
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create("seller-001", "sellerinsight-store")
        );

        String csv = """
                orderNo,orderItemNo,orderedAt,orderStatus,productId,productName,quantity,unitPrice,itemAmount,totalAmount,salePrice,stockQuantity,productStatus
                ORD-001,ITEM-001,2026-04-16T10:00:00+09:00,PAID,PROD-001,테스트 상품,2,15000,30000,30000,15000,10,ON_SALE
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "orders.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(
                        multipart("/api/v1/sellers/{sellerId}/import-jobs/orders/csv", seller.getId())
                                .file(file)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.totalRowCount").value(1))
                .andExpect(jsonPath("$.data.successRowCount").value(1))
                .andExpect(jsonPath("$.data.failedRowCount").value(0));

        assertThat(productRepository.count()).isEqualTo(1);
        assertThat(customerOrderRepository.count()).isEqualTo(1);
        assertThat(orderItemRepository.count()).isEqualTo(1);
        assertThat(importJobRepository.count()).isEqualTo(1);

        ImportJob importJob = importJobRepository.findAll().get(0);

        mockMvc.perform(
                        get("/api/v1/sellers/{sellerId}/import-jobs/{importJobId}", seller.getId(), importJob.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(importJob.getId()))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    void importOrdersCsvMarksFailedWhenHeadersAreInvalid() throws Exception {
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create("seller-001", "sellerinsight-store")
        );

        String csv = """
                orderNo,orderedAt,orderStatus,productId,productName,quantity,unitPrice,itemAmount,totalAmount,salePrice,stockQuantity,productStatus
                ORD-001,2026-04-16T10:00:00+09:00,PAID,PROD-001,테스트 상품,2,15000,30000,30000,15000,10,ON_SALE
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid-orders.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(
                        multipart("/api/v1/sellers/{sellerId}/import-jobs/orders/csv", seller.getId())
                                .file(file)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.totalRowCount").value(0))
                .andExpect(jsonPath("$.data.successRowCount").value(0))
                .andExpect(jsonPath("$.data.failedRowCount").value(0))
                .andExpect(jsonPath("$.data.errorMessage").value("CSV 파일이 비어 있거나 형식이 올바르지 않습니다."));

        assertThat(productRepository.count()).isZero();
        assertThat(customerOrderRepository.count()).isZero();
        assertThat(orderItemRepository.count()).isZero();
        assertThat(importJobRepository.count()).isEqualTo(1);
    }
}
