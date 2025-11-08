package com.ucamp.project.repository;

import com.ucamp.project.model.Post;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.model.Transcription;
import jakarta.transaction.Transactional;
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

    void deleteAllBySimulation(Simulation simulation);

    void deleteAllBySimulation_SimulationId(Long simulationId);

    long countBySimulation(Simulation simulation);
    @Query(value = """
    SELECT COUNT(*)
    FROM TRANSCRIPTION t
    WHERE t.SIMULATION_ID = :simulationId
     AND DBMS_LOB.GETLENGTH(
           REGEXP_REPLACE(NVL(t.TR_ANSWER_TEXT, ' '), '^[[:space:]]+|[[:space:]]+$', '')
        ) > 0
    """, nativeQuery = true)
    long countBySimulation_SimulationId(Long simulationId);

    @Modifying
    @Transactional
    @Query(value = """
    DELETE FROM TRANSCRIPTION t
    WHERE t.SIMULATION_ID IN (
        SELECT s.SIMULATION_ID FROM SIMULATION s
        WHERE (s.SIMULATION_STATUS = 'INPROGRESS' OR s.SIMULATION_COMPLETED_AT IS NULL)
          AND s.SIMULATION_CREATED_AT < (SYSDATE - INTERVAL '2' HOUR)
    )
    """, nativeQuery = true)
    int deleteByInvalidSimulations();


    @Query(value = """
    SELECT COUNT(*)
      FROM TRANSCRIPTION t
     WHERE t.SIMULATION_ID = :simulationId
       AND (
            DBMS_LOB.GETLENGTH(REGEXP_REPLACE(NVL(t.FEEDBACK, ' '), '^[[:space:]]+|[[:space:]]+$', '')) > 0
         OR DBMS_LOB.GETLENGTH(REGEXP_REPLACE(NVL(t.TR_ANSWER_TEXT, ' '), '^[[:space:]]+|[[:space:]]+$', '')) = 0
       )
""", nativeQuery = true)
    long countFeedbackIncludingSilent(@Param("simulationId") Long simulationId);
}