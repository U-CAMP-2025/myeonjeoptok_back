package com.ucamp.project.repository;

import com.ucamp.project.model.Post;
import com.ucamp.project.model.Simulation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SimulationRepository extends JpaRepository<Simulation, Long> {
    @EntityGraph(attributePaths = {"post", "post.qaList", "interviewer"})
    Optional<Simulation> findBySimulationId(Long simulationId);

    List<Simulation> findByUser_UserIdOrderBySimulationIdDesc(Long userId);


    @Query(value = """
            SELECT *
            FROM (
                SELECT s.*,
                       ROW_NUMBER() OVER (PARTITION BY s.post_id ORDER BY s.simulation_created_at DESC) AS rn
                FROM SIMULATION s
                WHERE s.user_id = :userId
                  AND s.simulation_status = 'SUCCESS'
            ) t
            WHERE t.rn = 1
            """, nativeQuery = true)
    List<Simulation> findLatestSimulationPerPost(@Param("userId") Long userId);

    @Query(value = """
        SELECT COUNT(*)
        FROM SIMULATION s
        WHERE s.user_id = :userId
          AND s.simulation_status = 'SUCCESS'
          AND s.post_id = :postId
        """, nativeQuery = true)
    Long countSuccessByUserAndPost(@Param("userId") Long userId,
                                   @Param("postId") Long postId);


    void deleteByPost(Post post);

    List<Simulation> findByPost(Post post);
}
