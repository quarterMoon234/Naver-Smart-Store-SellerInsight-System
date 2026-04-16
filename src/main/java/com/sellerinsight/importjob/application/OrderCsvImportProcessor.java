package com.sellerinsight.importjob.application;

import com.sellerinsight.common.error.BusinessException;
import com.sellerinsight.common.error.ErrorCode;
import com.sellerinsight.importjob.domain.ImportJob;
import com.sellerinsight.importjob.domain.ImportJobRepository;
import com.sellerinsight.order.domain.CustomerOrder;
import com.sellerinsight.order.domain.CustomerOrderRepository;
import com.sellerinsight.order.domain.OrderItem;
import com.sellerinsight.order.domain.OrderItemRepository;
import com.sellerinsight.product.domain.Product;
import com.sellerinsight.product.domain.ProductRepository;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OrderCsvImportProcessor {

    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "orderNo",
            "orderItemNo",
            "orderedAt",
            "orderStatus",
            "productId",
            "productName",
            "quantity",
            "unitPrice",
            "itemAmount",
            "totalAmount",
            "salePrice",
            "stockQuantity",
            "productStatus"
    );

    private final ImportJobRepository importJobRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;

    public void process(Long sellerId, Long importJobId, MultipartFile file) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        ImportJob importJob = importJobRepository.findByIdAndSellerId(importJobId, sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMPORT_JOB_NOT_FOUND));

        importJob.markProcessing();

        int totalRowCount = 0;

        try (
                Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreHeaderCase(false)
                        .setTrim(true)
                        .build()
                        .parse(reader)
        ) {
            validateHeaders(parser);

            for (CSVRecord record : parser) {
                totalRowCount++;
                importRecord(seller, record);
            }

            importJob.markSuccess(totalRowCount, totalRowCount, 0);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.CSV_IMPORT_FAILED);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.CSV_IMPORT_FAILED);
        }
    }

    private void validateHeaders(CSVParser parser) {
        Set<String> actualHeaders = parser.getHeaderMap().keySet();

        if (!actualHeaders.containsAll(REQUIRED_HEADERS)) {
            throw new BusinessException(ErrorCode.CSV_IMPORT_INVALID_FILE);
        }
    }

    private void importRecord(Seller seller, CSVRecord record) {
        Product product = upsertProduct(seller, record);
        CustomerOrder customerOrder = upsertOrder(seller, record);
        upsertOrderItem(customerOrder, product, record);
    }

    private Product upsertProduct(Seller seller, CSVRecord record) {
        String externalProductId = requireText(record, "productId");
        String productName = requireText(record, "productName");
        BigDecimal salePrice = parseBigDecimal(record, "salePrice");
        Integer stockQuantity = parseInteger(record, "stockQuantity");
        String productStatus = requireText(record, "productStatus");

        return productRepository.findBySellerIdAndExternalProductId(seller.getId(), externalProductId)
                .map(existingProduct -> {
                    existingProduct.updateFromImport(
                            productName,
                            salePrice,
                            stockQuantity,
                            productStatus
                    );
                    return existingProduct;
                })
                .orElseGet(() -> productRepository.save(
                        Product.create(
                                seller,
                                externalProductId,
                                productName,
                                salePrice,
                                stockQuantity,
                                productStatus
                        )
                ));
    }

    private CustomerOrder upsertOrder(Seller seller, CSVRecord record) {
        String externalOrderNo = requireText(record, "orderNo");
        OffsetDateTime orderedAt = parseOffsetDateTime(record, "orderedAt");
        String orderStatus = requireText(record, "orderStatus");
        BigDecimal totalAmount = parseBigDecimal(record, "totalAmount");

        return customerOrderRepository.findBySellerIdAndExternalOrderNo(seller.getId(), externalOrderNo)
                .map(existingOrder -> {
                    existingOrder.updateFromImport(
                            orderedAt,
                            orderStatus,
                            totalAmount
                    );
                    return existingOrder;
                })
                .orElseGet(() -> customerOrderRepository.save(
                        CustomerOrder.create(
                                seller,
                                externalOrderNo,
                                orderedAt,
                                orderStatus,
                                totalAmount
                        )
                ));
    }

    private void upsertOrderItem(
            CustomerOrder customerOrder,
            Product product,
            CSVRecord record
    ) {
        String externalOrderItemNo = requireText(record, "orderItemNo");
        Integer quantity = parseInteger(record, "quantity");
        BigDecimal unitPrice = parseBigDecimal(record, "unitPrice");
        BigDecimal itemAmount = parseBigDecimal(record, "itemAmount");

        orderItemRepository.findByCustomerOrderIdAndExternalOrderItemNo(
                        customerOrder.getId(),
                        externalOrderItemNo
                )
                .ifPresentOrElse(
                        existingOrderItem -> existingOrderItem.updateFromImport(
                                product,
                                quantity,
                                unitPrice,
                                itemAmount
                        ),
                        () -> orderItemRepository.save(
                                OrderItem.create(
                                        customerOrder,
                                        product,
                                        externalOrderItemNo,
                                        quantity,
                                        unitPrice,
                                        itemAmount
                                )
                        )
                );
    }

    private String requireText(CSVRecord record, String columnName) {
        String value = record.get(columnName);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(columnName + " 값이 비어 있습니다.");
        }

        return value;
    }

    private Integer parseInteger(CSVRecord record, String columnName) {
        return Integer.parseInt(requireText(record, columnName));
    }

    private BigDecimal parseBigDecimal(CSVRecord record, String columnName) {
        return new BigDecimal(requireText(record, columnName));
    }

    private OffsetDateTime parseOffsetDateTime(CSVRecord record, String columnName) {
        return OffsetDateTime.parse(requireText(record, columnName));
    }
}
