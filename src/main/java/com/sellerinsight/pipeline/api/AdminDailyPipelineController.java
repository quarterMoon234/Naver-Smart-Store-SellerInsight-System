package com.sellerinsight.pipeline.api;

import com.sellerinsight.common.api.ApiResponse;
import com.sellerinsight.pipeline.api.dto.DailyPipelineRunResponse;
import com.sellerinsight.pipeline.api.dto.PipelineRunDetailResponse;
import com.sellerinsight.pipeline.api.dto.PipelineRunSummaryResponse;
import com.sellerinsight.pipeline.application.DailyPipelineExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Tag(name = "Admin Pipeline", description = "운영용 일별 파이프라인 실행 API")
@RestController
@RequestMapping("/api/v1/admin/pipelines")
@RequiredArgsConstructor
public class AdminDailyPipelineController {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

    private final DailyPipelineExecutionService dailyPipelineExecutionService;

    @Operation(summary = "전체 판매자 대상 일별 집계 및 인사이트 생성 실행")
    @PostMapping("/daily")
    public ApiResponse<DailyPipelineRunResponse> runDailyPipeline(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        LocalDate targetDate = date != null
                ? date
                : LocalDate.now(ASIA_SEOUL).minusDays(1);

        return ApiResponse.ok(dailyPipelineExecutionService.run(targetDate));
    }

    @Operation(summary = "일별 파이프라인 실행 이력 목록 조회")
    @GetMapping("/daily/runs")
    public ApiResponse<List<PipelineRunSummaryResponse>> getRuns(
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(dailyPipelineExecutionService.getRuns(limit));
    }

    @Operation(summary = "일별 파이프라인 실행 이력 상세 조회")
    @GetMapping("/daily/runs/{runId}")
    public ApiResponse<PipelineRunDetailResponse> getRunDetail(@PathVariable Long runId) {
        return ApiResponse.ok(dailyPipelineExecutionService.getRunDetail(runId));
    }
}