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

}
