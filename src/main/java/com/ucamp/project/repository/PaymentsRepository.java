package com.ucamp.project.repository;

import com.ucamp.project.model.Payments;
import com.ucamp.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Long> {

    Optional<Payments> findByUserAndExpiredAtBeforeAndPaymentStatus(User user, LocalDateTime currentTime, String paymentStatus);
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

    Optional<Payments> findTopByUser_UserIdOrderByApprovedAtDesc(Long userId);

    List<Payments> findByUser_UserIdOrderByApprovedAtDesc(Long userId);
}
