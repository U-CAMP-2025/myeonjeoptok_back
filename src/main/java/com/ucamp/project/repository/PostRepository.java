package com.ucamp.project.repository;

import com.ucamp.project.model.Post;
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
        SELECT p.post_id as postId,
               u.nickname as nickname,
               LISTAGG(j.job_name, ',') WITHIN GROUP (ORDER BY j.job_name) as job,
               p.post_title as title,
               p.post_description as description,
               p.post_import_count as bookCount,
               COUNT(r.review_id) as review,
               p.post_created_at as createAt,
               CASE WHEN p.post_status = 'Y' THEN 1 ELSE 0 END as isPublic,
               0 as isPassed
        FROM post p
        LEFT JOIN users u ON p.user_id = u.user_id
        LEFT JOIN post_job pj ON pj.post_id = p.post_id
        LEFT JOIN job j ON pj.job_id = j.job_id
        LEFT JOIN review r ON r.post_id = p.post_id
        WHERE p.post_status = 'Y'
          AND (:jobIds IS NULL OR pj.job_id IN :jobIds)
        GROUP BY p.post_id, u.nickname, p.post_title, p.post_description,
                 p.post_import_count, p.post_created_at, p.post_status
        """,
            countQuery = """
        SELECT COUNT(DISTINCT p.post_id)
        FROM post p
        LEFT JOIN post_job pj ON pj.post_id = p.post_id
        WHERE p.post_status = 'Y'
          AND (:jobIds IS NULL OR pj.job_id IN :jobIds)
        """,
            nativeQuery = true
    )
    Page<Object[]> findPostsWithJoins(@Param("jobIds") List<Long> jobIds, Pageable pageable);


}
