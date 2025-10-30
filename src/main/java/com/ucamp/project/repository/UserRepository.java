package com.ucamp.project.repository;

import com.ucamp.project.dto.UserResponse;
import com.ucamp.project.dto.UserWithCertDTO;
import com.ucamp.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
//

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(Long userId);

    @Query("""
        SELECT new com.ucamp.project.dto.UserResponse(
            u.nickname,
            u.email,
            j.jobId,
            j.jobName,
            u.passStatus,
            u.createdAt,
            u.role,
            s.simulationStatus,
            s.simulationCompletedAt,
            c.certStatus,
            c.certReqDate,
            c.certTrmtDate
        )
        FROM User u
        LEFT JOIN u.job j
        LEFT JOIN Certificate c ON c.user.userId = u.userId
        LEFT JOIN Simulation s ON s.user.userId = u.userId
        ORDER BY u.createdAt DESC
    """)
    List<UserResponse> findAllWithCertAndSimulInfo();

    @Query("""
    SELECT new com.ucamp.project.dto.UserWithCertDTO(
            u.nickname,
            u.email,
            u.job.jobId,
            u.job.jobName,
            u.passStatus,
            c.certStatus,
            c.certFileUrl,
            c.certReqDate,
            c.certTrmtDate
        )
        FROM User u
        INNER JOIN Certificate c ON c.user.userId = u.userId
    """)
    List<UserWithCertDTO> findAllWithCert();

}
