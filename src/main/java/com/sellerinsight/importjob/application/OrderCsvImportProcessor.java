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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
        int successRowCount = 0;

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
                successRowCount++;
            }

            importJob.markSuccess(totalRowCount, successRowCount, 0);
        } catch (IOException exception) {
            throw new CsvImportProcessingException(
                    totalRowCount,
                    successRowCount,
                    calculateFailedRowCount(totalRowCount, successRowCount),
                    ErrorCode.CSV_IMPORT_FAILED.getMessage(),
                    exception
            );
        } catch (BusinessException | IllegalArgumentException exception) {
            throw new CsvImportProcessingException(
                    totalRowCount,
                    successRowCount,
                    calculateFailedRowCount(totalRowCount, successRowCount),
                    exception.getMessage(),
                    exception
            );
        }
    }

    private void validateHeaders(CSVParser parser) {
        Set<String> actualHeaders = parser.getHeaderMap().keySet();

        if (actualHeaders.size() != REQUIRED_HEADERS.size() || !actualHeaders.containsAll(REQUIRED_HEADERS)) {
            throw new BusinessException(ErrorCode.CSV_IMPORT_INVALID_FILE);
        }
    }

    private void importRecord(Seller seller, CSVRecord record) {
        OffsetDateTime orderedAt = parseOffsetDateTime(record, "orderedAt");

        Product product = upsertProduct(seller, record, orderedAt);
        CustomerOrder customerOrder = upsertOrder(seller, record, orderedAt);
        upsertOrderItem(customerOrder, product, record);
    }

    private Product upsertProduct(Seller seller, CSVRecord record, OffsetDateTime orderedAt) {
        String externalProductId = requireText(record, "productId");
        String productName = requireText(record, "productName");
        BigDecimal salePrice = parseNonNegativeBigDecimal(record, "salePrice");
        Integer stockQuantity = parseNonNegativeInteger(record, "stockQuantity");
        String productStatus = requireText(record, "productStatus");

        return productRepository.findBySellerIdAndExternalProductId(seller.getId(), externalProductId)
                .map(existingProduct -> {
                    existingProduct.updateFromImport(
                            orderedAt,
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
                                productStatus,
                                orderedAt
                        )
                ));
    }

    private CustomerOrder upsertOrder(Seller seller, CSVRecord record, OffsetDateTime orderedAt) {
        String externalOrderNo = requireText(record, "orderNo");
        String orderStatus = requireText(record, "orderStatus");
        BigDecimal totalAmount = parseNonNegativeBigDecimal(record, "totalAmount");

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
        Integer quantity = parsePositiveInteger(record, "quantity");
        BigDecimal unitPrice = parseNonNegativeBigDecimal(record, "unitPrice");
        BigDecimal itemAmount = parseNonNegativeBigDecimal(record, "itemAmount");

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
            throw invalidValue(record, columnName + " 값이 비어 있습니다.");
        }

        return value;
    }

    private Integer parsePositiveInteger(CSVRecord record, String columnName) {
        int value = parseInteger(record, columnName);

        if (value <= 0) {
            throw invalidValue(record, columnName + " 값은 0보다 커야 합니다.");
        }

        return value;
    }

    private Integer parseNonNegativeInteger(CSVRecord record, String columnName) {
        int value = parseInteger(record, columnName);

        if (value < 0) {
            throw invalidValue(record, columnName + " 값은 0 이상이어야 합니다.");
        }

        return value;
    }

    private int parseInteger(CSVRecord record, String columnName) {
        try {
            return Integer.parseInt(requireText(record, columnName));
        } catch (NumberFormatException exception) {
            throw invalidValue(record, columnName + " 값은 정수여야 합니다.");
        }
    }

    private BigDecimal parseNonNegativeBigDecimal(CSVRecord record, String columnName) {
        try {
            BigDecimal value = new BigDecimal(requireText(record, columnName));

            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw invalidValue(record, columnName + " 값은 0 이상이어야 합니다.");
            }

            return value;
        } catch (NumberFormatException exception) {
            throw invalidValue(record, columnName + " 값은 숫자여야 합니다.");
        }
    }

    private OffsetDateTime parseOffsetDateTime(CSVRecord record, String columnName) {
        try {
            return OffsetDateTime.parse(requireText(record, columnName));
        } catch (DateTimeParseException exception) {
            throw invalidValue(record, columnName + " 값은 ISO-8601 OffsetDateTime 형식이어야 합니다.");
        }
    }

    private IllegalArgumentException invalidValue(CSVRecord record, String message) {
        return new IllegalArgumentException("행 " + record.getRecordNumber() + ": " + message);
    }

    private int calculateFailedRowCount(int totalRowCount, int successRowCount) {
        return totalRowCount > successRowCount ? 1 : 0;
    }
}
