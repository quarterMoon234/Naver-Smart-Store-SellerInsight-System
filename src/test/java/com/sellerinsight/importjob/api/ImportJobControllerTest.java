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
import com.sellerinsight.product.domain.Product;
import com.sellerinsight.product.domain.ProductRepository;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerCredentialRepository;
import com.sellerinsight.seller.domain.SellerRepository;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
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

    @Test
    void importOrdersCsvRejectsInvalidExtension() throws Exception {
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create("seller-001", "sellerinsight-store")
        );

        String csv = """
                orderNo,orderItemNo,orderedAt,orderStatus,productId,productName,quantity,unitPrice,itemAmount,totalAmount,salePrice,stockQuantity,productStatus
                ORD-001,ITEM-001,2026-04-16T10:00:00+09:00,PAID,PROD-001,테스트 상품,2,15000,30000,30000,15000,10,ON_SALE
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "orders.txt",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(
                        multipart("/api/v1/sellers/{sellerId}/import-jobs/orders/csv", seller.getId())
                                .file(file)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("IMPORT_400_3"));

        assertThat(importJobRepository.count()).isZero();
    }

    @Test
    void importOrdersCsvRejectsUnsupportedContentType() throws Exception {
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
                "application/json",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(
                        multipart("/api/v1/sellers/{sellerId}/import-jobs/orders/csv", seller.getId())
                                .file(file)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("IMPORT_400_3"));

        assertThat(importJobRepository.count()).isZero();
    }

    @Test
    void importOrdersCsvRejectsTooLargeFile() throws Exception {
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create("seller-001", "sellerinsight-store")
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "orders.csv",
                "text/csv",
                new byte[5 * 1024 * 1024 + 1]
        );

        mockMvc.perform(
                        multipart("/api/v1/sellers/{sellerId}/import-jobs/orders/csv", seller.getId())
                                .file(file)
                )
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("IMPORT_413_1"));

        assertThat(importJobRepository.count()).isZero();
    }

    @Test
    void importOrdersCsvMarksFailedWhenQuantityIsNegative() throws Exception {
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create("seller-001", "sellerinsight-store")
        );

        String csv = """
                orderNo,orderItemNo,orderedAt,orderStatus,productId,productName,quantity,unitPrice,itemAmount,totalAmount,salePrice,stockQuantity,productStatus
                ORD-001,ITEM-001,2026-04-16T10:00:00+09:00,PAID,PROD-001,테스트 상품,-1,15000,30000,30000,15000,10,ON_SALE
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
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.totalRowCount").value(1))
                .andExpect(jsonPath("$.data.successRowCount").value(0))
                .andExpect(jsonPath("$.data.failedRowCount").value(1))
                .andExpect(jsonPath("$.data.errorMessage").value("행 1: quantity 값은 0보다 커야 합니다."));
    }

    @Test
    void importOrdersCsvKeepsLatestProductStateWhenOlderCsvIsReuploaded() throws Exception {
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create("seller-001", "sellerinsight-store")
        );

        String latestCsv = """
                orderNo,orderItemNo,orderedAt,orderStatus,productId,productName,quantity,unitPrice,itemAmount,totalAmount,salePrice,stockQuantity,productStatus
                ORD-NEW-001,ITEM-NEW-001,2026-04-20T10:00:00+09:00,PAID,PROD-001,최신 상품명,1,15000,15000,15000,15000,10,ON_SALE
                """;

        String oldCsv = """
                orderNo,orderItemNo,orderedAt,orderStatus,productId,productName,quantity,unitPrice,itemAmount,totalAmount,salePrice,stockQuantity,productStatus
                ORD-OLD-001,ITEM-OLD-001,2026-04-19T10:00:00+09:00,PAID,PROD-001,과거 상품명,1,15000,15000,15000,15000,0,SOLD_OUT
                """;

        MockMultipartFile latestFile = new MockMultipartFile(
                "file",
                "latest-orders.csv",
                "text/csv",
                latestCsv.getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile oldFile = new MockMultipartFile(
                "file",
                "old-orders.csv",
                "text/csv",
                oldCsv.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(
                        multipart("/api/v1/sellers/{sellerId}/import-jobs/orders/csv", seller.getId())
                                .file(latestFile)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        mockMvc.perform(
                        multipart("/api/v1/sellers/{sellerId}/import-jobs/orders/csv", seller.getId())
                                .file(oldFile)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        Product product = productRepository.findBySellerIdAndExternalProductId(seller.getId(), "PROD-001")
                .orElseThrow();

        assertThat(product.getProductName()).isEqualTo("최신 상품명");
        assertThat(product.getStockQuantity()).isEqualTo(10);
        assertThat(product.getProductStatus()).isEqualTo("ON_SALE");
        assertThat(product.getSalePrice()).isEqualByComparingTo("15000");
        assertThat(product.getLastImportedAt())
                .isEqualTo(OffsetDateTime.parse("2026-04-20T10:00:00+09:00"));
    }
}
