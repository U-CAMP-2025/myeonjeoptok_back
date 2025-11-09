package com.ucamp.project.repository;

import com.ucamp.project.dto.*;
import com.ucamp.project.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
//

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = """
    SELECT 
        u.user_id AS userId,
        u.nickname,
        u.email,
        j.job_id AS jobId,
        j.job_name AS jobName,
        u.pass_status AS passStatus,
        u.status,
        u.users_profile_image_url AS userProfileImageUrl,
        (
            SELECT c.cert_status
            FROM certificate c
            WHERE c.user_id = u.user_id
            AND c.cert_req_date = (
                SELECT MAX(c2.cert_req_date)
                FROM certificate c2
                WHERE c2.user_id = u.user_id
            )
        ) AS certStatus
    FROM users u
    LEFT JOIN job j ON u.job_id = j.job_id
    WHERE u.user_id = :userId
""", nativeQuery = true)
    Map<String, Object> findUserWithLatestCertStatus(@Param("userId") Long userId);

    Optional<User> findByUserId(Long userId);

    @Query(value = """
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
            c.certTrmtDate,
            c.certFileUrl
        )
        FROM User u
        LEFT JOIN u.job j
        LEFT JOIN Certificate c ON c.certReqDate = (
            SELECT MAX(c2.certReqDate)
            FROM Certificate c2
            WHERE c2.user.userId = u.userId
        )
        LEFT JOIN Simulation s ON s.simulationCompletedAt = (
            SELECT MAX(s2.simulationCompletedAt)
            FROM Simulation s2
            WHERE s2.user.userId = u.userId
        )
        ORDER BY u.createdAt DESC
    """)
    Page<UserResponse> findAllWithCertAndSimulInfo(Pageable pageable);

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
    Optional<User> findByKakaoId(String kakaoId);
    Optional<User> findByNickname(String nickname);


    // 시뮬레이션 변환 처리 상태 현황 조회.
    // simulation에 저장된 qa_count와 (같은 simulation_id에 대한) transcription의 개수(COUNT(*))가 동일하면 SUCCESS, 아니라면 INPROGRESS.
    @Query(value = """
        SELECT
            p.post_title AS title,
            u.nickname   AS nickname,
            u.email      AS email,
            TO_CHAR(s1.simulation_completed_at,'YYYY-MM-DD HH24:MI:SS') AS completed_at,
            CASE
                WHEN s1.simulation_qa_count >= COALESCE(s2.tr_count, 0) THEN 'SUCCESS'
                ELSE 'INPROGRESS'
            END AS status
        FROM simulation s1
        INNER JOIN users u ON s1.user_id = u.user_id
        INNER JOIN post  p ON s1.post_id = p.post_id
        LEFT JOIN (
            SELECT t.simulation_id, COUNT(*) AS tr_count
            FROM transcription t
            GROUP BY t.simulation_id
        ) s2 ON s1.simulation_id = s2.simulation_id
        ORDER BY s1.simulation_completed_at DESC
        """,
            countQuery = """
        SELECT COUNT(1)
        FROM simulation s1
        INNER JOIN users u ON s1.user_id = u.user_id
        INNER JOIN post  p ON s1.post_id = p.post_id
        """,
            nativeQuery = true)
    Page<UserWithSimulDTO> findAllTranscriptionStatus(Pageable pageable);
    boolean existsByNicknameIgnoreCase(String nickname);
    Optional<User> findByNicknameIgnoreCase(String nickname);

    @Query(value = """
            SELECT
                u.user_id AS userId,
                u.nickname AS nickname,
                u.pass_status AS passStatus,
                u.users_profile_image_url AS usersProfileImageUrl,
                j.job_name AS jobName,
                COALESCE(SUM(p.post_import_count),0) AS cnt
            FROM users u
            JOIN job j ON u.job_id = j.job_id
            JOIN post p ON u.user_id = p.user_id
            GROUP BY u.user_id, u.nickname, u.pass_status, u.users_profile_image_url, j.job_name
            HAVING SUM(p.post_import_count) > 0
            ORDER BY cnt DESC
            """, nativeQuery = true)
    List<Object[]> findAllBookmark();

///
    @Query(value = """
            SELECT
                u.user_id AS userId,
                 u.nickname AS nickname,
                 u.pass_status AS passStatus,
                 u.users_profile_image_url AS usersProfileImageUrl,
                 j.job_name AS jobName,
                 COUNT(s.simulation_id) AS cnt
             FROM users u
             JOIN job j ON u.job_id = j.job_id
             JOIN simulation s ON s.user_id = u.user_id
             WHERE s.simulation_status = 'SUCCESS'
             AND (
                 (:period = 'thisweek' AND s.simulation_completed_at BETWEEN TRUNC(SYSDATE, 'D') AND TRUNC(SYSDATE, 'D') + 7) OR
                 (:period = 'thismonth' AND s.simulation_completed_at BETWEEN TRUNC(SYSDATE, 'MM') AND ADD_MONTHS(TRUNC(SYSDATE, 'MM'), 1)) OR
                 (:period NOT IN ('thisweek', 'thismonth'))
             )
             GROUP BY u.user_id, u.nickname, u.pass_status, u.users_profile_image_url, j.job_name
             ORDER BY cnt DESC
            """, nativeQuery = true)
    List<Object[]> findAllPractice(String period);

    @Query(value = """
            SELECT
                u.user_id AS userId,
                u.nickname AS nickname,
                u.email AS email,
                u.users_profile_image_url AS usersProfileImageUrl,
                u.pass_status AS passStatus,
                j.job_name AS jobName
            FROM users u
            LEFT JOIN job j ON u.job_id = j.job_id
            WHERE u.user_id = :userId
            """, nativeQuery = true)
    UserDetailDto findUserDetailById(@Param("userId") Long userId);

}
