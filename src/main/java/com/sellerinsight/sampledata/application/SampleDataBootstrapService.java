package com.sellerinsight.sampledata.application;

import com.sellerinsight.common.error.BusinessException;
import com.sellerinsight.common.error.ErrorCode;
import com.sellerinsight.importjob.domain.ImportJobRepository;
import com.sellerinsight.insight.domain.InsightRepository;
import com.sellerinsight.insight.domain.RecommendationRepository;
import com.sellerinsight.metric.domain.DailyMetricRepository;
import com.sellerinsight.order.domain.CustomerOrder;
import com.sellerinsight.order.domain.CustomerOrderRepository;
import com.sellerinsight.order.domain.OrderItem;
import com.sellerinsight.order.domain.OrderItemRepository;
import com.sellerinsight.pipeline.domain.PipelineRunItemRepository;
import com.sellerinsight.pipeline.domain.PipelineRunRepository;
import com.sellerinsight.pipeline.domain.PipelineExecutionLockRepository;
import com.sellerinsight.product.domain.Product;
import com.sellerinsight.product.domain.ProductRepository;
import com.sellerinsight.sampledata.api.dto.SampleDataBootstrapResponse;
import com.sellerinsight.sampledata.api.dto.SampleDataSellerResponse;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerCredentialRepository;
import com.sellerinsight.seller.domain.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SampleDataBootstrapService {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");
    private static final String DEFAULT_SCENARIO = "default";
    private static final String LARGE_SCENARIO = "large";
    private static final int LARGE_SELLER_COUNT = 30;
    private static final int LARGE_PRODUCT_COUNT_PER_SELLER = 20;
    private static final int LARGE_PREVIOUS_ORDER_COUNT_PER_SELLER = 100;
    private static final int LARGE_TARGET_ORDER_COUNT_PER_SELLER = 60;

    private final PipelineRunItemRepository pipelineRunItemRepository;
    private final PipelineRunRepository pipelineRunRepository;
    private final PipelineExecutionLockRepository pipelineExecutionLockRepository;
    private final RecommendationRepository recommendationRepository;
    private final InsightRepository insightRepository;
    private final DailyMetricRepository dailyMetricRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final ProductRepository productRepository;
    private final ImportJobRepository importJobRepository;
    private final SellerCredentialRepository sellerCredentialRepository;
    private final SellerRepository sellerRepository;

    public SampleDataBootstrapResponse bootstrap(String scenario) {
        String normalizedScenario = scenario == null ? DEFAULT_SCENARIO : scenario.trim().toLowerCase();

        if (!DEFAULT_SCENARIO.equals(normalizedScenario) && !LARGE_SCENARIO.equals(normalizedScenario)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        clearExistingData();

        LocalDate targetMetricDate = LocalDate.now(ASIA_SEOUL).minusDays(1);
        LocalDate previousMetricDate = targetMetricDate.minusDays(1);
        LocalDate recentMetricDate = targetMetricDate.minusDays(10);
        LocalDate staleMetricDate = targetMetricDate.minusDays(20);

        if (LARGE_SCENARIO.equals(normalizedScenario)) {
            return seedLargeScenario(
                    normalizedScenario,
                    previousMetricDate,
                    targetMetricDate
            );
        }

        SellerSeedResult alpha = seedAlphaSeller(
                previousMetricDate,
                targetMetricDate,
                recentMetricDate,
                staleMetricDate
        );
        SellerSeedResult beta = seedBetaSeller(
                previousMetricDate,
                targetMetricDate,
                recentMetricDate
        );

        List<SampleDataSellerResponse> sellers = List.of(
                new SampleDataSellerResponse(
                        alpha.seller().getId(),
                        alpha.seller().getExternalSellerId(),
                        alpha.seller().getSellerName(),
                        alpha.productCount(),
                        alpha.orderCount()
                ),
                new SampleDataSellerResponse(
                        beta.seller().getId(),
                        beta.seller().getExternalSellerId(),
                        beta.seller().getSellerName(),
                        beta.productCount(),
                        beta.orderCount()
                )
        );

        return new SampleDataBootstrapResponse(
                normalizedScenario,
                previousMetricDate,
                targetMetricDate,
                sellers.size(),
                alpha.productCount() + beta.productCount(),
                alpha.orderCount() + beta.orderCount(),
                alpha.orderItemCount() + beta.orderItemCount(),
                sellers
        );
    }

    private void clearExistingData() {
        pipelineExecutionLockRepository.deleteAllInBatch();
        pipelineRunItemRepository.deleteAllInBatch();
        pipelineRunRepository.deleteAllInBatch();
        recommendationRepository.deleteAllInBatch();
        insightRepository.deleteAllInBatch();
        dailyMetricRepository.deleteAllInBatch();
        orderItemRepository.deleteAllInBatch();
        customerOrderRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        importJobRepository.deleteAllInBatch();
        sellerCredentialRepository.deleteAllInBatch();
        sellerRepository.deleteAllInBatch();
    }

    private SellerSeedResult seedAlphaSeller(
            LocalDate previousMetricDate,
            LocalDate targetMetricDate,
            LocalDate recentMetricDate,
            LocalDate staleMetricDate
    ) {
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create("sample-seller-alpha", "sample-store-alpha")
        );

        Product mainProduct = productRepository.saveAndFlush(
                Product.create(
                        seller,
                        "ALPHA-PROD-001",
                        "Alpha 베스트셀러",
                        new BigDecimal("15000"),
                        12,
                        "ON_SALE",
                        importedAt(previousMetricDate)
                )
        );
        Product soldOutProduct = productRepository.saveAndFlush(
                Product.create(
                        seller,
                        "ALPHA-PROD-002",
                        "Alpha 품절 상품",
                        new BigDecimal("23000"),
                        0,
                        "SOLD_OUT",
                        importedAt(previousMetricDate)
                )
        );
        Product staleProduct = productRepository.saveAndFlush(
                Product.create(
                        seller,
                        "ALPHA-PROD-003",
                        "Alpha 장기 미판매 상품",
                        new BigDecimal("18000"),
                        7,
                        "ON_SALE",
                        importedAt(staleMetricDate)
                )
        );

        int orderCount = 0;
        int orderItemCount = 0;

        for (int sequence = 1; sequence <= 3; sequence++) {
            createSingleItemOrder(
                    seller,
                    mainProduct,
                    previousMetricDate,
                    sequence,
                    10 + sequence,
                    1,
                    new BigDecimal("15000")
            );
            orderCount++;
            orderItemCount++;
        }

        for (int sequence = 4; sequence <= 5; sequence++) {
            createSingleItemOrder(
                    seller,
                    soldOutProduct,
                    previousMetricDate,
                    sequence,
                    10 + sequence,
                    1,
                    new BigDecimal("23000")
            );
            orderCount++;
            orderItemCount++;
        }

        for (int sequence = 6; sequence <= 7; sequence++) {
            createSingleItemOrder(
                    seller,
                    mainProduct,
                    targetMetricDate,
                    sequence,
                    11 + sequence,
                    1,
                    new BigDecimal("15000")
            );
            orderCount++;
            orderItemCount++;
        }

        createSingleItemOrder(
                seller,
                mainProduct,
                recentMetricDate,
                8,
                15,
                1,
                new BigDecimal("15000")
        );
        orderCount++;
        orderItemCount++;

        createSingleItemOrder(
                seller,
                staleProduct,
                staleMetricDate,
                9,
                13,
                1,
                new BigDecimal("18000")
        );
        orderCount++;
        orderItemCount++;

        return new SellerSeedResult(seller, 3, orderCount, orderItemCount);
    }

    private SellerSeedResult seedBetaSeller(
            LocalDate previousMetricDate,
            LocalDate targetMetricDate,
            LocalDate recentMetricDate
    ) {
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create("sample-seller-beta", "sample-store-beta")
        );

        Product mainProduct = productRepository.saveAndFlush(
                Product.create(
                        seller,
                        "BETA-PROD-001",
                        "Beta 대표 상품",
                        new BigDecimal("12000"),
                        14,
                        "ON_SALE",
                        importedAt(previousMetricDate)
                )
        );
        Product secondaryProduct = productRepository.saveAndFlush(
                Product.create(
                        seller,
                        "BETA-PROD-002",
                        "Beta 보조 상품",
                        new BigDecimal("9000"),
                        20,
                        "ON_SALE",
                        importedAt(previousMetricDate)
                )
        );

        int orderCount = 0;
        int orderItemCount = 0;

        createSingleItemOrder(
                seller,
                mainProduct,
                previousMetricDate,
                1,
                10,
                1,
                new BigDecimal("12000")
        );
        orderCount++;
        orderItemCount++;

        createSingleItemOrder(
                seller,
                secondaryProduct,
                previousMetricDate,
                2,
                11,
                1,
                new BigDecimal("9000")
        );
        orderCount++;
        orderItemCount++;

        createSingleItemOrder(
                seller,
                mainProduct,
                targetMetricDate,
                3,
                12,
                1,
                new BigDecimal("12000")
        );
        orderCount++;
        orderItemCount++;

        createSingleItemOrder(
                seller,
                secondaryProduct,
                targetMetricDate,
                4,
                13,
                1,
                new BigDecimal("9000")
        );
        orderCount++;
        orderItemCount++;

        createSingleItemOrder(
                seller,
                mainProduct,
                recentMetricDate,
                5,
                14,
                1,
                new BigDecimal("12000")
        );
        orderCount++;
        orderItemCount++;

        return new SellerSeedResult(seller, 2, orderCount, orderItemCount);
    }

    private SampleDataBootstrapResponse seedLargeScenario(
            String normalizedScenario,
            LocalDate previousMetricDate,
            LocalDate targetMetricDate
    ) {
        List<SampleDataSellerResponse> sellers = new ArrayList<>();

        int totalProductCount = 0;
        int totalOrderCount = 0;
        int totalOrderItemCount = 0;

        for (int sellerIndex = 1; sellerIndex <= LARGE_SELLER_COUNT; sellerIndex++) {
            SellerSeedResult result = seedLargeSeller(
                    sellerIndex,
                    previousMetricDate,
                    targetMetricDate
            );

            sellers.add(new SampleDataSellerResponse(
                    result.seller().getId(),
                    result.seller().getExternalSellerId(),
                    result.seller().getSellerName(),
                    result.productCount(),
                    result.orderCount()
            ));

            totalProductCount += result.productCount();
            totalOrderCount += result.orderCount();
            totalOrderItemCount += result.orderItemCount();
        }

        return new SampleDataBootstrapResponse(
                normalizedScenario,
                previousMetricDate,
                targetMetricDate,
                sellers.size(),
                totalProductCount,
                totalOrderCount,
                totalOrderItemCount,
                sellers
        );
    }

    private SellerSeedResult seedLargeSeller(
            int sellerIndex,
            LocalDate previousMetricDate,
            LocalDate targetMetricDate
    ) {
        String sellerSequence = "%03d".formatted(sellerIndex);
        Seller seller = sellerRepository.saveAndFlush(
                Seller.create(
                        "large-seller-" + sellerSequence,
                        "large-store-" + sellerSequence
                )
        );

        List<Product> products = new ArrayList<>();

        for (int productIndex = 1; productIndex <= LARGE_PRODUCT_COUNT_PER_SELLER; productIndex++) {
            String productSequence = "%03d".formatted(productIndex);
            boolean soldOut = productIndex <= 3;

            products.add(Product.create(
                    seller,
                    seller.getExternalSellerId() + "-PROD-" + productSequence,
                    "Large 상품 " + sellerSequence + "-" + productSequence,
                    new BigDecimal("20000"),
                    soldOut ? 0 : 100 + productIndex,
                    soldOut ? "SOLD_OUT" : "ON_SALE",
                    importedAt(previousMetricDate)
            ));
        }

        List<Product> savedProducts = productRepository.saveAll(products);

        int orderCount = 0;
        int orderItemCount = 0;

        for (int sequence = 1; sequence <= LARGE_PREVIOUS_ORDER_COUNT_PER_SELLER; sequence++) {
            Product product = savedProducts.get((sequence - 1) % 10);

            createLargeSingleItemOrder(
                    seller,
                    product,
                    previousMetricDate,
                    "PREV",
                    sequence,
                    1,
                    new BigDecimal("20000")
            );

            orderCount++;
            orderItemCount++;
        }

        for (int sequence = 1; sequence <= LARGE_TARGET_ORDER_COUNT_PER_SELLER; sequence++) {
            Product product = savedProducts.get(3 + ((sequence - 1) % 7));

            createLargeSingleItemOrder(
                    seller,
                    product,
                    targetMetricDate,
                    "TARGET",
                    sequence,
                    1,
                    new BigDecimal("10000")
            );

            orderCount++;
            orderItemCount++;
        }

        return new SellerSeedResult(
                seller,
                LARGE_PRODUCT_COUNT_PER_SELLER,
                orderCount,
                orderItemCount
        );
    }

    private void createSingleItemOrder(
            Seller seller,
            Product product,
            LocalDate orderDate,
            int sequence,
            int hour,
            int quantity,
            BigDecimal unitPrice
    ) {
        BigDecimal itemAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        OffsetDateTime orderedAt = orderDate.atTime(hour, 0)
                .atZone(ASIA_SEOUL)
                .toOffsetDateTime();

        CustomerOrder customerOrder = customerOrderRepository.saveAndFlush(
                CustomerOrder.create(
                        seller,
                        seller.getExternalSellerId() + "-ORD-" + sequence + "-" + orderDate,
                        orderedAt,
                        "PAID",
                        itemAmount
                )
        );

        orderItemRepository.saveAndFlush(
                OrderItem.create(
                        customerOrder,
                        product,
                        seller.getExternalSellerId() + "-ITEM-" + sequence + "-" + orderDate,
                        quantity,
                        unitPrice,
                        itemAmount
                )
        );
    }

    private void createLargeSingleItemOrder(
            Seller seller,
            Product product,
            LocalDate orderDate,
            String orderGroup,
            int sequence,
            int quantity,
            BigDecimal unitPrice
    ) {
        BigDecimal itemAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        OffsetDateTime orderedAt = orderDate.atTime(sequence % 24, sequence % 60)
                .atZone(ASIA_SEOUL)
                .toOffsetDateTime();

        CustomerOrder customerOrder = customerOrderRepository.save(
                CustomerOrder.create(
                        seller,
                        seller.getExternalSellerId() + "-ORD-" + orderGroup + "-" + sequence,
                        orderedAt,
                        "PAID",
                        itemAmount
                )
        );

        orderItemRepository.save(
                OrderItem.create(
                        customerOrder,
                        product,
                        seller.getExternalSellerId() + "-ITEM-" + orderGroup + "-" + sequence,
                        quantity,
                        unitPrice,
                        itemAmount
                )
        );
    }

    private OffsetDateTime importedAt(LocalDate metricDate) {
        return metricDate.atStartOfDay(ASIA_SEOUL).toOffsetDateTime();
    }

    private record SellerSeedResult(
            Seller seller,
            int productCount,
            int orderCount,
            int orderItemCount
    ) {
    }
}
