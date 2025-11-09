package com.ucamp.project.repository;

import com.ucamp.project.model.SimulationUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface SimulationUsageRepository extends JpaRepository<SimulationUsage, Long> {
    long countByUser_UserIdAndSimDate(Long userId, LocalDate simDate);
    boolean existsByUser_UserIdAndSimulationId(Long userId, Long simulationId);

}
