package com.sellerinsight.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(Environment environment) {
        String profile = Arrays.stream(environment.getActiveProfiles())
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse("default");

        String applicationName = environment.getProperty("spring.application.name", "sellerinsight");

        return registry -> registry.config()
                .commonTags("application", applicationName, "profile", profile)
                .meterFilter(MeterFilter.denyNameStartsWith("tomcat.sessions"));
    }
}
