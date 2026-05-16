package com.taskmanager.api.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Error response returned by the API.")
public class ErrorResponse {

    @Schema(description = "Error category.", example = "BAD_REQUEST")
    private String error;

    @Schema(description = "Human-readable error message.", example = "Invalid request")
    private String message;

    @Schema(description = "Field-level validation errors, when available.")
    private Map<String, String> fields;
}
