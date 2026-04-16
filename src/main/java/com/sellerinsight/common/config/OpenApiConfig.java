package com.sellerinsight.common.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sellerInsightOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Seller Insight API")
                        .description("네이버 커머스 연동 판매자 인사이트 백엔드 API")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("sellerinsight")
                                .email("dev@sellerinsight.local")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Overview")
                        .url("https://github.com/your-org/sellerinsight"));
    }
}
