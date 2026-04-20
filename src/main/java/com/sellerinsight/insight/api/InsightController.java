package com.sellerinsight.insight.api;

import com.sellerinsight.common.api.ApiResponse;
import com.sellerinsight.insight.api.dto.InsightResponse;
import com.sellerinsight.insight.api.dto.InsightsByDateResponse;
import com.sellerinsight.insight.application.InsightGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Insight", description = "인사이트 생성 및 조회 API")
@RestController
@RequestMapping("/api/v1/sellers/{sellerId}/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightGenerationService insightGenerationService;

    @Operation(summary = "특정 날짜 인사이트 생성")
    @PostMapping("/generate")
    public ApiResponse<InsightsByDateResponse> generate(
            @PathVariable Long sellerId,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ApiResponse.ok(insightGenerationService.generate(sellerId, date));
    }

    @Operation(summary = "특정 날짜 인사이트 조회")
    @GetMapping
    public ApiResponse<InsightsByDateResponse> getByDate(
            @PathVariable Long sellerId,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ApiResponse.ok(insightGenerationService.getInsightsByDate(sellerId, date));
    }

    @Operation(summary = "인사이트 상세 조회")
    @GetMapping("/{insightId}")
    public ApiResponse<InsightResponse> getInsight(
            @PathVariable Long sellerId,
            @PathVariable Long insightId
    ) {
        return ApiResponse.ok(insightGenerationService.getInsight(sellerId, insightId));
    }
}
