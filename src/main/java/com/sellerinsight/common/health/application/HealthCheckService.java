package com.sellerinsight.common.health.application;

import com.sellerinsight.common.error.BusinessException;
import com.sellerinsight.common.error.ErrorCode;
import com.sellerinsight.common.health.dto.HealthCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    public HealthCheckResponse check() {
        Integer result = jdbcTemplate.queryForObject("select 1", Integer.class);

        if (result == null || result != 1) {
            throw new BusinessException(ErrorCode.DB_CONNECTION_FAILED);
        }

        return new HealthCheckResponse(
                environment.getProperty("spring.application.name", "sellerinsight"),
                "UP",
                "UP",
                Arrays.asList(environment.getActiveProfiles()),
                OffsetDateTime.now(ASIA_SEOUL)
        );
    }
}
