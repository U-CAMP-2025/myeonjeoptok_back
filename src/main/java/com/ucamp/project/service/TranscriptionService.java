package com.ucamp.project.service;

import com.ucamp.project.model.Qa;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.model.Transcription;
import com.ucamp.project.repository.TranscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TranscriptionService {
    private final TranscriptionRepository transcriptionRepository;

    @Transactional
    public Transcription upsert(Long simulationId, Long qaId, String text) {
        Transcription tr = transcriptionRepository.findBySimulation_SimulationIdAndQa_QaId(simulationId, qaId)
                .orElseGet(() -> Transcription.builder()
                        .simulation(Simulation.builder().simulationId(simulationId).build())
                        .qa(Qa.builder().qaId(qaId).build())
                        .build());
        tr.setTrAnswerText(text);
        tr.setCompletedAt(LocalDateTime.now());
        return transcriptionRepository.save(tr);
    }

    @Transactional(readOnly = true)
    public List<Transcription> findAllBySimulation(Long simulationId) {
        return transcriptionRepository.findAllBySimulation_SimulationId(simulationId);
    }
}