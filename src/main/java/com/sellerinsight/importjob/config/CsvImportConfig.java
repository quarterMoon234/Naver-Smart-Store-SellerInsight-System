package com.sellerinsight.importjob.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CsvImportProperties.class)
public class CsvImportConfig {
}
