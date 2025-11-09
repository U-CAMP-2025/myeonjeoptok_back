package com.ucamp.project.repository;

import com.ucamp.project.model.Post;
import com.ucamp.project.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE user.userId = :userId")
    List<Post> simulGetPost(@Param("userId") Long userId);


    Optional<Post> findByUserUserIdAndPostId(Long userId, Long postId);

    @Query(
            value = """
                    SELECT 
                        p.post_id AS postId,
                        u.nickname AS nickname,
                        LISTAGG(DISTINCT j.job_name, ',') WITHIN GROUP (ORDER BY j.job_name) AS job,
                        p.post_title AS title,
                        p.post_description AS description,
                        p.post_import_count AS bookCount,
                        COUNT(DISTINCT r.review_id) AS review,
                        p.post_created_at AS createAt,
                        CASE WHEN p.post_status = 'Y' THEN 1 ELSE 0 END AS isPublic,
                        0 AS isPassed
                    FROM post p
                    LEFT JOIN users u ON p.user_id = u.user_id
                    LEFT JOIN post_job pj ON pj.post_id = p.post_id
                    LEFT JOIN job j ON pj.job_id = j.job_id
                    LEFT JOIN review r ON r.post_id = p.post_id
                    WHERE p.post_status = 'Y'
                      AND (p.post_id IN (
                              SELECT DISTINCT pj2.post_id
                              FROM post_job pj2
                              WHERE pj2.job_id IN (:jobIds)
                          )
                      )
                    GROUP BY 
                        p.post_id, u.nickname, p.post_title, p.post_description,
                        p.post_import_count, p.post_created_at, p.post_status
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT p.post_id)
                    FROM post p
                    WHERE p.post_status = 'Y'
                      AND (p.post_id IN (
                              SELECT DISTINCT pj2.post_id
                              FROM post_job pj2
                              WHERE pj2.job_id IN (:jobIds)
                          )
                      )
                    """,
            nativeQuery = true
    )
    Page<Object[]> findPostsWithJoins(@Param("jobIds") List<Long> jobIds, Pageable pageable);

    @Query(
            value = """
                    SELECT 
                        p.post_id AS postId,
                        u.nickname AS nickname,
                        LISTAGG(DISTINCT j.job_name, ',') WITHIN GROUP (ORDER BY j.job_name) AS job,
                        p.post_title AS title,
                        p.post_description AS description,
                        p.post_import_count AS bookCount,
                        COUNT(DISTINCT r.review_id) AS review,
                        p.post_created_at AS createAt,
                        CASE WHEN p.post_status = 'Y' THEN 1 ELSE 0 END AS isPublic,
                        0 AS isPassed
                    FROM post p
                    LEFT JOIN users u ON p.user_id = u.user_id
                    LEFT JOIN post_job pj ON pj.post_id = p.post_id
                    LEFT JOIN job j ON pj.job_id = j.job_id
                    LEFT JOIN review r ON r.post_id = p.post_id
                    WHERE p.post_status = 'Y'
                      AND (
                          :jobIds IS NULL
                      )
                    GROUP BY 
                        p.post_id, u.nickname, p.post_title, p.post_description,
                        p.post_import_count, p.post_created_at, p.post_status
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT p.post_id)
                    FROM post p
                    WHERE p.post_status = 'Y'
                      AND (
                          :jobIds IS NULL
                      )
                    """,
            nativeQuery = true
    )
    Page<Object[]> findPostsWithJoinsNotJobs(@Param("jobIds") List<Long> jobIds, Pageable pageable);

    // DB에 저장 완료 후 응답 반환
    @Query("""
    select p from Post p
    left join fetch p.qaList q
    where p.postId = :postId
""")
    Optional<Post> findByIdFetchQa(@Param("postId") Long postId);

    int countByUser(User user);

    @Query(value = """
            SELECT
                p.post_id AS postId,
                p.post_title AS postTitle,
                p.post_description AS postDescription,
                p.post_import_count AS bookmarkCount,
                TO_CHAR(p.post_created_at, 'YYYY-MM-DD') AS postCreatedAt,
                COUNT(r.review_id) AS reviewCount
            FROM post p
            LEFT JOIN review r ON p.post_id = r.post_id
            WHERE p.user_id = :userId
            AND p.post_status = 'Y'
            GROUP BY p.post_id, p.post_title, p.post_description, p.post_import_count, p.post_created_at
            ORDER BY p.post_created_at DESC
            """, nativeQuery = true)
    List<Object []> findPostDetailById(@Param("userId") Long userId);
}