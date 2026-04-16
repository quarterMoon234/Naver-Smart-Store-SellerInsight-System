package com.sellerinsight.common.health.api;

import com.sellerinsight.common.api.ApiResponse;
import com.sellerinsight.common.health.application.HealthCheckService;
import com.sellerinsight.common.health.dto.HealthCheckResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health", description = "헬스체크 API")
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    @Operation(summary = "애플리케이션 및 DB 상태 조회")
    @GetMapping
    public ApiResponse<HealthCheckResponse> health() {
        return ApiResponse.ok(healthCheckService.check());
    }
}