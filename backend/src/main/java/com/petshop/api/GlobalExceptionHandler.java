package com.petshop.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        ApiErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), ex.getMessage(), null));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        ApiErrorCode errorCode;
        switch (ex.getStatus()) {
            case BAD_REQUEST:
                errorCode = ApiErrorCode.INVALID_PARAM;
                break;
            case UNAUTHORIZED:
                errorCode = ApiErrorCode.UNAUTHORIZED;
                break;
            case FORBIDDEN:
                errorCode = ApiErrorCode.FORBIDDEN;
                break;
            case NOT_FOUND:
                errorCode = ApiErrorCode.NOT_FOUND;
                break;
            default:
                errorCode = ApiErrorCode.SYSTEM_ERROR;
                break;
        }
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), ex.getReason(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        return ResponseEntity.status(ApiErrorCode.SYSTEM_ERROR.getHttpStatus())
                .body(ApiResponse.failure(ApiErrorCode.SYSTEM_ERROR.getCode(), ApiErrorCode.SYSTEM_ERROR.getMessage(), null));
    }
}
