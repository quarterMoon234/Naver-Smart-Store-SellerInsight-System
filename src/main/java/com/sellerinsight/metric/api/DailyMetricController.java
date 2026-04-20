package com.sellerinsight.metric.api;

import com.sellerinsight.common.api.ApiResponse;
import com.sellerinsight.metric.api.dto.DailyMetricResponse;
import com.sellerinsight.metric.application.DailyMetricAggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Daily Metric", description = "일별 지표 집계 API")
@RestController
@RequestMapping("/api/v1/sellers/{sellerId}/daily-metrics")
@RequiredArgsConstructor
public class DailyMetricController {

    private final DailyMetricAggregationService dailyMetricAggregationService;

    @Operation(summary = "일별 지표 수동 집계 실행")
    @PostMapping("/aggregate")
    public ApiResponse<DailyMetricResponse> aggregate(
            @PathVariable Long sellerId,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ApiResponse.ok(dailyMetricAggregationService.aggregate(sellerId, date));
    }

    @Operation(summary = "일별 지표 단건 조회")
    @GetMapping("/{metricDate}")
    public ApiResponse<DailyMetricResponse> get(
            @PathVariable Long sellerId,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate metricDate
    ) {
        return ApiResponse.ok(dailyMetricAggregationService.get(sellerId, metricDate));
    }
}
