package com.taskmanager.api.exception;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// import org.jspecify.annotations.Nullable;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.taskmanager.api.dto.ErrorResponse;
import com.taskmanager.api.entity.TaskPriority;
import com.taskmanager.api.entity.TaskStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFoundException(TaskNotFoundException ex) {

        ErrorResponse error = new ErrorResponse("NOT_FOUND", ex.getMessage(), null);

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return new ResponseEntity<>(
                new ErrorResponse("SERVER_ERROR", "An unexpected error occurred", null),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidTaskException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTaskException(InvalidTaskException ex) {

        ErrorResponse error = new ErrorResponse("BAD_REQUEST", ex.getMessage(), null);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Handles invalid query parameters that send a
    // MethodArgumentTypeMismatchException
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {

        return new ResponseEntity<>(
                new ErrorResponse("BAD_REQUEST", "The " + ex
                        .getName() + " - " + ex.getValue() + " is invalid.", null),
                HttpStatus.BAD_REQUEST);

    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));

        return new ResponseEntity<>(new ErrorResponse("VALIDATION ERROR", "Invalid request", fields),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles JSON parsing and type conversion failures.
     *
     * This method is triggered BEFORE bean validation runs.
     *
     * It catches cases such as:
     * - Invalid enum values (e.g. "status": "INVALID")
     * - Malformed JSON
     * - Type mismatches (e.g. invalid LocalDate format)
     *
     * These are considered BAD_REQUEST errors (structural issues),
     * not VALIDATION_ERROR (which are bean validation failures).
     *
     * Validation failures are handled separately in handleMethodArgumentNotValid().
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        /*
         * An Exception can sometimes be wrapped in another Exception.
         * This method walks the exception chain internally and returns the deepest
         * (root) exception
         */
        log.info("ex class: {}", ex.getClass().getName());
        log.info("ex.getCause(): {}", ex.getCause());

        log.info("ex cause", ex.getCause());
        log.info("mostSpecific: {}",
                NestedExceptionUtils.getMostSpecificCause(ex).getClass().getName());

        InvalidFormatException cause = findInvalidFormat(ex);

        log.info("found InvalidFormatException? {}", cause != null);

        if (cause != null) {

            Class<?> targetType = cause.getTargetType();

            // System.out.println("Path size: " + formatException.getPath().size());

            if (targetType != null && targetType.isEnum()) {

                String fieldName = "values";

                Object[] allowedValues = targetType.getEnumConstants();

                Map<String, String> fields = new HashMap<>();

                fields.put(fieldName, "Allowed values: " +
                        Arrays.toString(allowedValues));

                // log.info("Enum mismatch detected for field: {}", fieldName);

                return new ResponseEntity<>(new ErrorResponse("BAD_REQUEST",
                        "The input " + cause.getValue() + " is invalid.",
                        fields),
                        HttpStatus.BAD_REQUEST);

            }
        }

        // Handles invalid JSON inputs
        if (ex.getCause() instanceof JsonParseException) {
            return new ResponseEntity<>(new ErrorResponse("BAD_REQUEST", "Invalid JSON",
                    null), HttpStatus.BAD_REQUEST);
        }

        Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);
        log.info("Most specific cause: {}", root.getClass().getName());

        // let other mismatches fall back to generic handling
        return new ResponseEntity<>(new ErrorResponse("BAD_REQUEST", "Invalid input",
                null), HttpStatus.BAD_REQUEST);
    }

    private InvalidFormatException findInvalidFormat(Throwable ex) {
        Throwable cur = ex;
        while (cur != null) {
            if (cur instanceof InvalidFormatException ife)
                return ife;

            cur = cur.getCause();
        }
        return null;
    }
}
