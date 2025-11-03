package com.ucamp.project.repository;

import com.ucamp.project.model.Transcription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TranscriptionRepository extends JpaRepository<Transcription, Long> {
    Optional<Transcription> findBySimulation_SimulationIdAndQa_QaId(Long simulationId, Long qaId);
    List<Transcription> findAllBySimulation_SimulationId(Long simulationId);
}