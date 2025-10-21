package com.healthcare.patient_service.dto;

public class PaymentDtos {
    public record PaymentIntentResponse(String clientSecret, String currency, long amount) {}
}
