package com.sellerinsight.common.health.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record HealthCheckResponse (
        String application,
        String status,
        String database,
        List<String> activeProfiles,
        OffsetDateTime timestamp
) {
}
