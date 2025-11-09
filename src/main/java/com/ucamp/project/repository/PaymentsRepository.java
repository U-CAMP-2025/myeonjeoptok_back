package com.ucamp.project.repository;

import com.ucamp.project.model.Payments;
import com.ucamp.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = "SELECT p.* FROM payments p " +
            "WHERE p.payment_status = 'ACTIVE' " +
            "AND p.expired_at BETWEEN :startDate AND :endDate " +
            "AND p.expired_at = (" +
            "    SELECT MAX(p2.expired_at) " +
            "    FROM payments p2 " +
            "    WHERE p2.user_id = p.user_id " +
            "    AND p2.payment_status = 'ACTIVE' " +
            "    AND p2.expired_at BETWEEN :startDate AND :endDate" +
            ")",
            nativeQuery = true)
    List<Payments> findActivePaymentsWithMaxExpirationNative(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    Optional<Payments> findTopByUser_UserIdOrderByApprovedAtDesc(Long userId);

    List<Payments> findByUser_UserIdOrderByExpiredAtDesc(Long userId);

    Optional<Payments> findTopByUser_UserIdOrderByExpiredAtDesc(Long userId);
}
