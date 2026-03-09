package com.taskmanager.api.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private String error;

    private String message;

    private Map<String, String> fields;
}
