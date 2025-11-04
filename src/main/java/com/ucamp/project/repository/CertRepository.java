package com.ucamp.project.repository;

import com.ucamp.project.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByUserUserId(@Param("userId") Long userId);

    Optional<Certificate> findTopByUserUserIdOrderByCertReqDateDesc(Long userId);

}
