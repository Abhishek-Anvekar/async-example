package com.healthcare.doctor_service.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String m){ super(m); }
}
