package com.ucamp.project.repository;

import com.ucamp.project.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
//

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Query(value = """
            SELECT
                j.job_id AS jobId,
                j.job_name AS jobName
            FROM job j
            JOIN post_job pj ON j.job_id = pj.job_id
            JOIN post p ON p.post_id = pj.post_id
            WHERE p.post_id = :postId
            """, nativeQuery = true)
    List<Object[]> findAllById(Long postId);
}
