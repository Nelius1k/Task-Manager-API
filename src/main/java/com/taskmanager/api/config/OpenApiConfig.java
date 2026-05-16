package com.taskmanager.api.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Task Manager API", version = "v1", description = "REST API for managing tasks."))
public class OpenApiConfig {
}
