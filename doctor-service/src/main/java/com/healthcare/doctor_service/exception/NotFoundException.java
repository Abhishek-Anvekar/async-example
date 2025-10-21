package com.healthcare.doctor_service.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String m){ super(m); }
}
