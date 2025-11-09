package com.ucamp.project.repository;

import com.ucamp.project.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payments, Long> {

    Optional<Payments> findTopByUser_UserIdOrderByApprovedAtDesc(Long userId);

    List<Payments> findByUser_UserIdOrderByApprovedAtDesc(Long userId);
}
