package com.sellerinsight.importjob.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.import.csv")
public record CsvImportProperties(
        @NotNull DataSize maxFileSize,
        @NotEmpty List<String> allowedContentTypes
) {
}
