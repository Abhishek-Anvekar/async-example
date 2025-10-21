package com.healthcare.doctor_service.exception;

import java.time.OffsetDateTime;

public record ApiError(
        String path, int status, String error, String message,
        OffsetDateTime timestamp) {}
