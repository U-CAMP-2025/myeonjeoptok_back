package com.ucamp.project.repository;

import com.ucamp.project.model.Payments;
import com.ucamp.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Long> {

    Optional<Payments> findByUserAndExpiredAtBeforeAndPaymentStatus(User user, LocalDateTime currentTime, String paymentStatus);

}
