package com.sellerinsight.importjob.api;

import com.sellerinsight.common.api.ApiResponse;
import com.sellerinsight.importjob.api.dto.ImportJobResponse;
import com.sellerinsight.importjob.application.OrderCsvImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Import Job", description = "CSV 가져오기 API")
@RestController
@RequestMapping("/api/v1/sellers/{sellerId}/import-jobs")
@RequiredArgsConstructor
public class ImportJobController {

    private final OrderCsvImportService orderCsvImportService;

    @Operation(summary = "주문 CSV 업로드 및 동기 적재")
    @PostMapping(value = "/orders/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImportJobResponse> importOrdersCsv(
            @PathVariable Long sellerId,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.ok(orderCsvImportService.importCsv(sellerId, file));
    }

    @Operation(summary = "가져오기 작업 단건 조회")
    @GetMapping("/{importJobId}")
    public ApiResponse<ImportJobResponse> getImportJob(
            @PathVariable Long sellerId,
            @PathVariable Long importJobId
    ) {
        return ApiResponse.ok(orderCsvImportService.getImportJob(sellerId, importJobId));
    }
}
