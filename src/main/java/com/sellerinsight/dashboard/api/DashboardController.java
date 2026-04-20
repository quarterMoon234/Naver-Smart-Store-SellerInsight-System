package com.sellerinsight.dashboard.api;

import com.sellerinsight.common.api.ApiResponse;
import com.sellerinsight.dashboard.api.dto.DashboardMetricRangeResponse;
import com.sellerinsight.dashboard.api.dto.DashboardResponse;
import com.sellerinsight.dashboard.api.dto.RecentInsightsResponse;
import com.sellerinsight.dashboard.application.DashboardQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Dashboard", description = "판매자 대시보드 조회 API")
@RestController
@RequestMapping("/api/v1/sellers/{sellerId}/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardQueryService dashboardQueryService;

    @Operation(summary = "판매자 대시보드 조회")
    @GetMapping
    public ApiResponse<DashboardResponse> getDashboard(
            @PathVariable Long sellerId,
            @RequestParam(value = "metricDays", defaultValue = "7") int metricDays,
            @RequestParam(value = "insightLimit", defaultValue = "5") int insightLimit
    ) {
        return ApiResponse.ok(
                dashboardQueryService.getDashboard(sellerId, metricDays, insightLimit)
        );
    }

    @Operation(summary = "대시보드용 일별 지표 범위 조회")
    @GetMapping("/metrics")
    public ApiResponse<DashboardMetricRangeResponse> getMetricRange(
            @PathVariable Long sellerId,
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        return ApiResponse.ok(
                dashboardQueryService.getMetricRange(sellerId, from, to)
        );
    }

    @Operation(summary = "대시보드용 최근 인사이트 조회")
    @GetMapping("/insights/recent")
    public ApiResponse<RecentInsightsResponse> getRecentInsights(
            @PathVariable Long sellerId,
            @RequestParam(value = "limit", defaultValue = "5") int limit
    ) {
        return ApiResponse.ok(
                dashboardQueryService.getRecentInsights(sellerId, limit)
        );
    }
}
