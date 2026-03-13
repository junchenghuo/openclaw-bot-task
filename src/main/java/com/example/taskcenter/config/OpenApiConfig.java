package com.example.taskcenter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskCenterOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Task Center API")
                .version("v1")
                .description("简化版任务中台 API 文档"));
    }
}
