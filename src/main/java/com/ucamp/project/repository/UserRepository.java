package com.ucamp.project.repository;

import com.ucamp.project.dto.UserResponse;
import com.ucamp.project.dto.UserWithCertDTO;
import com.ucamp.project.dto.UserWithSimulDTO;
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
            u.userId,
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


    // 시뮬레이션 변환 처리 상태 현황 조회.
    // simulation에 저장된 qa_count와 (같은 simulation_id에 대한) transcription의 개수(COUNT(*))가 동일하면 SUCCESS, 아니라면 INPROGRESS.
    @Query(value = """
        SELECT
            p.post_title AS title,
            u.nickname AS nickname,
            u.email AS email,
            TO_CHAR(s1.simulation_completed_at,'YYYY-MM-DD HH24:MI:SS') AS completed_at,
            CASE
                WHEN s1.simulation_qa_count <= COALESCE(s2.tr_count, 0) THEN 'SUCCESS'
                ELSE 'INPROGRESS'
            END AS status
        FROM
            simulation s1
        INNER JOIN
            users u ON s1.user_id = u.user_id
        INNER JOIN
            post p ON s1.post_id = p.post_id
        LEFT JOIN
            (
                SELECT 
                    t.simulation_id, 
                    COUNT(*) AS tr_count
                FROM 
                    transcription t
                GROUP BY 
                    t.simulation_id
            ) s2 ON s1.simulation_id = s2.simulation_id
        """, nativeQuery = true)
    List<UserWithSimulDTO> findAllTranscriptionStatus();
}
