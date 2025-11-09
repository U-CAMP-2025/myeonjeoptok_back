package com.ucamp.project.repository;

import com.ucamp.project.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payments, Long> {

    @Query("""
    select count(p) > 0 from Payments p
        where p.user.userId = :userId
            and p.paymentStatus = 'ACTIVE'
            and p.expiredAt > :now
    """)
    boolean hasActivePayment(Long userId, LocalDateTime now);

    List<Payments> findByPaymentStatusAndExpiredAtBetween(
            String paymentStatus, LocalDateTime from, LocalDateTime to
    );
}
