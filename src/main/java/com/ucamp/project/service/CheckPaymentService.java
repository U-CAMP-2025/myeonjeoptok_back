package com.ucamp.project.service;

import com.ucamp.project.repository.PaymentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckPaymentService {
    private final PaymentsRepository paymentsRepository;

    public boolean isPayment(Long userId){
        return paymentsRepository.hasActivePayment(userId, LocalDateTime.now());
    }

}
