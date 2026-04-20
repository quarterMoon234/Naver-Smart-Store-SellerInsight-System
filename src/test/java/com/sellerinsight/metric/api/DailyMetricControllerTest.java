package com.sellerinsight.metric.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sellerinsight.importjob.domain.ImportJobRepository;
import com.sellerinsight.insight.domain.InsightRepository;
import com.sellerinsight.insight.domain.RecommendationRepository;
import com.sellerinsight.metric.domain.DailyMetricRepository;
import com.sellerinsight.order.domain.CustomerOrder;
import com.sellerinsight.order.domain.CustomerOrderRepository;
import com.sellerinsight.order.domain.OrderItem;
import com.sellerinsight.order.domain.OrderItemRepository;
import com.sellerinsight.product.domain.Product;
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
import java.time.OffsetDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DailyMetricControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private InsightRepository insightRepository;

    @BeforeEach
    void setUp() {
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
    void aggregateDailyMetric() throws Exception {
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create("seller-001", "sellerinsight-store")
        );

        Product soldOutProduct = productRepository.saveAndFlush(
                Product.create(
                        seller,
                        "PROD-001",
                        "품절 상품",
                        new BigDecimal("30000"),
                        0,
                        "SOLD_OUT"
                )
        );

        Product activeProduct = productRepository.saveAndFlush(
                Product.create(
                        seller,
                        "PROD-002",
                        "판매중 상품",
                        new BigDecimal("15000"),
                        12,
                        "ON_SALE"
                )
        );

        Product staleProduct = productRepository.saveAndFlush(
                Product.create(
                        seller,
                        "PROD-003",
                        "장기 미판매 상품",
                        new BigDecimal("20000"),
                        7,
                        "ON_SALE"
                )
        );

        CustomerOrder todayOrder1 = customerOrderRepository.saveAndFlush(
                CustomerOrder.create(
                        seller,
                        "ORD-001",
                        OffsetDateTime.parse("2026-04-16T10:00:00+09:00"),
                        "PAID",
                        new BigDecimal("30000")
                )
        );

        CustomerOrder todayOrder2 = customerOrderRepository.saveAndFlush(
                CustomerOrder.create(
                        seller,
                        "ORD-002",
                        OffsetDateTime.parse("2026-04-16T15:00:00+09:00"),
                        "PAID",
                        new BigDecimal("15000")
                )
        );

        CustomerOrder oldOrder = customerOrderRepository.saveAndFlush(
                CustomerOrder.create(
                        seller,
                        "ORD-003",
                        OffsetDateTime.parse("2026-03-20T11:00:00+09:00"),
                        "PAID",
                        new BigDecimal("20000")
                )
        );

        orderItemRepository.saveAndFlush(
                OrderItem.create(
                        todayOrder1,
                        soldOutProduct,
                        "ITEM-001",
                        1,
                        new BigDecimal("30000"),
                        new BigDecimal("30000")
                )
        );

        orderItemRepository.saveAndFlush(
                OrderItem.create(
                        todayOrder2,
                        activeProduct,
                        "ITEM-002",
                        1,
                        new BigDecimal("15000"),
                        new BigDecimal("15000")
                )
        );

        orderItemRepository.saveAndFlush(
                OrderItem.create(
                        oldOrder,
                        staleProduct,
                        "ITEM-003",
                        1,
                        new BigDecimal("20000"),
                        new BigDecimal("20000")
                )
        );

        mockMvc.perform(
                        post("/api/v1/sellers/{sellerId}/daily-metrics/aggregate", seller.getId())
                                .param("date", "2026-04-16")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.metricDate").value("2026-04-16"))
                .andExpect(jsonPath("$.data.orderCount").value(2))
                .andExpect(jsonPath("$.data.salesAmount").value(45000))
                .andExpect(jsonPath("$.data.averageOrderAmount").value(22500))
                .andExpect(jsonPath("$.data.soldOutProductCount").value(1))
                .andExpect(jsonPath("$.data.staleProductCount").value(1));

        mockMvc.perform(
                        get("/api/v1/sellers/{sellerId}/daily-metrics/{metricDate}", seller.getId(), "2026-04-16")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.metricDate").value("2026-04-16"))
                .andExpect(jsonPath("$.data.orderCount").value(2))
                .andExpect(jsonPath("$.data.salesAmount").value(45000))
                .andExpect(jsonPath("$.data.averageOrderAmount").value(22500))
                .andExpect(jsonPath("$.data.soldOutProductCount").value(1))
                .andExpect(jsonPath("$.data.staleProductCount").value(1));
    }
}
