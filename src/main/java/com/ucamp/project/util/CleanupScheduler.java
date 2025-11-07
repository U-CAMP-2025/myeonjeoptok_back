package com.ucamp.project.util;

import com.ucamp.project.repository.SimulationRepository;
import com.ucamp.project.repository.TranscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupScheduler {
    private final SimulationRepository simulationRepository;
    private final TranscriptionRepository transcriptionRepository;

    // 3분(180초)마다 실행
    @Scheduled(cron = "0 0 3 * * *")
//    @Scheduled(fixedRate = 360000)
    public void cleanupSimulations() {
        // 연관된 Transcription 먼저 삭제
        int deletedTrans = transcriptionRepository.deleteByInvalidSimulations();

        // 그다음 Simulation 삭제
        int deletedSim = simulationRepository.deleteInProgressOrIncomplete();

        if (deletedSim > 0 || deletedTrans > 0) {
            log.info("[시뮬레이션 정리] Simulation {}건, Transcription {}건 삭제됨",
                    deletedSim, deletedTrans);
        }
    }
}
