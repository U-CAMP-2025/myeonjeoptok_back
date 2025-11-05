package com.ucamp.project.repository;

import com.ucamp.project.model.Post;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.model.Transcription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TranscriptionRepository extends JpaRepository<Transcription, Long> {
    Optional<Transcription> findBySimulation_SimulationIdAndQa_QaId(Long simulationId, Long qaId);
    List<Transcription> findAllBySimulation_SimulationId(Long simulationId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE TRANSCRIPTION
           SET QA_ID = :newQaId
         WHERE QA_ID = :oldQaId
    """, nativeQuery = true)
    int reassignAllQa(@Param("oldQaId") Long oldQaId, @Param("newQaId") Long newQaId);

    boolean existsByQa_QaId(Long qaQaId);
    void deleteBySimulation(Simulation sim);

    void deleteAllByQaQaId(Long qaId);
}