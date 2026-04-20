package com.sellerinsight.common.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.api-security")
public record ApiSecurityProperties(
        @NotBlank String adminUsername,
        @NotBlank String adminPassword,
        @NotBlank String sellerUsername,
        @NotBlank String sellerPassword
) {
}
