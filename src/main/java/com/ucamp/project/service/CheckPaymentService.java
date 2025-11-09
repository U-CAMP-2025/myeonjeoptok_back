package com.ucamp.project.service;

import com.ucamp.project.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckPaymentService {
    private final PaymentRepository paymentRepository;

    public boolean isPayment(Long userId){
        return paymentRepository.hasActivePayment(userId, LocalDateTime.now());
    }

}
