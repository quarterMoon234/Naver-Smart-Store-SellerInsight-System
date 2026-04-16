package com.sellerinsight.importjob.application;

import com.sellerinsight.common.error.BusinessException;
import com.sellerinsight.common.error.ErrorCode;
import com.sellerinsight.importjob.api.dto.ImportJobResponse;
import com.sellerinsight.importjob.domain.ImportJob;
import com.sellerinsight.importjob.domain.ImportJobRepository;
import com.sellerinsight.importjob.domain.ImportJobType;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OrderCsvImportService {

    private final SellerRepository sellerRepository;
    private final ImportJobRepository importJobRepository;
    private final OrderCsvImportProcessor orderCsvImportProcessor;
    private final TransactionTemplate transactionTemplate;

    public ImportJobResponse importCsv(Long sellerId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.CSV_IMPORT_INVALID_FILE);
        }

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        ImportJob importJob = transactionTemplate.execute(status ->
                importJobRepository.saveAndFlush(
                        ImportJob.create(
                                seller,
                                ImportJobType.ORDER_CSV,
                                file.getOriginalFilename() == null ? "orders.csv" : file.getOriginalFilename()
                        )
                )
        );

        if (importJob == null) {
            throw new BusinessException(ErrorCode.CSV_IMPORT_FAILED);
        }

        try {
            transactionTemplate.executeWithoutResult(status ->
                    orderCsvImportProcessor.process(sellerId, importJob.getId(), file)
            );
        } catch (Exception exception) {
            transactionTemplate.executeWithoutResult(status -> {
                ImportJob failedJob = importJobRepository.findByIdAndSellerId(importJob.getId(), sellerId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.IMPORT_JOB_NOT_FOUND));
                failedJob.markFailed(resolveErrorMessage(exception));
            });
        }

        return getImportJob(sellerId, importJob.getId());
    }

    public ImportJobResponse getImportJob(Long sellerId, Long importJobId) {
        ImportJob importJob = importJobRepository.findByIdAndSellerId(importJobId, sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMPORT_JOB_NOT_FOUND));

        return ImportJobResponse.from(importJob);
    }

    private String resolveErrorMessage(Exception exception) {
        if (exception instanceof BusinessException businessException) {
            return businessException.getMessage();
        }

        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }

        return ErrorCode.CSV_IMPORT_FAILED.getMessage();
    }
}
