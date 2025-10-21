package com.healthcare.patient_service.service;

import com.healthcare.patient_service.dto.PaymentDtos.PaymentIntentResponse;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public PaymentIntentResponse createIntent(long amount, String currency){
        // TODO: integrate real payment gateway (Stripe/Paytm)
        // For demo return a fake client secret
        return new PaymentIntentResponse("demo_client_secret_"+System.currentTimeMillis(), currency, amount);
    }
}
