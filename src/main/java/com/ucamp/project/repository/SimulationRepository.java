package com.ucamp.project.repository;

import com.ucamp.project.model.Post;
import com.ucamp.project.model.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulationRepository extends JpaRepository<Simulation, Long> {
}
