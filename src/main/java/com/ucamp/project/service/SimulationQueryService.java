package com.ucamp.project.service;

import com.ucamp.project.dto.*;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.model.Transcription;
import com.ucamp.project.repository.SimulationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimulationQueryService {
    private final SimulationService simulationService;
    private final TranscriptionService transcriptionService;
    private final SimulationRepository  simulationRepository;

    @Transactional(readOnly = true)
    public SimulationResultDto buildResult(Long simulationId) {
        // 1) 시뮬 상세
        SimulationDetailResponse detail = simulationService.findDetail(simulationId);

        // 2) 해당 시뮬의 모든 Transcription을 한 번에 가져와
        //    qaId -> (가장 최근) Transcription 으로 매핑
        Map<Long, Transcription> trByQaId = transcriptionService.findAllBySimulation(simulationId).stream()
                .filter(t -> t.getQa() != null && t.getQa().getQaId() != null)
                .sorted(Comparator.comparing(Transcription::getCompletedAt,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.toMap(
                        t -> t.getQa().getQaId(),
                        t -> t,
                        (prev, curr) -> curr // 동일 qaId면 더 최근 것으로 덮기
                ));

        // 3) Qa 리스트를 DTO로 변환하며 transcript + feedback을 같이 주입
        List<QaDto> mapped = detail.getPost().getQaList().stream()
                .map(qa -> {
                    Transcription tr = trByQaId.get(qa.getQaId());
                    String transcript = (tr == null) ? "" : Optional.ofNullable(tr.getTrAnswerText()).orElse("");
                    String feedback   = (tr == null) ? "" : Optional.ofNullable(tr.getFeedback()).orElse("");

                    return QaDto.builder()
                            .qaId(qa.getQaId())
                            .qaOrder(qa.getQaOrder())
                            .qaQuestion(qa.getQaQuestion())
                            .qaAnswer(qa.getQaAnswer())
                            .transContent(transcript) // STT
                            .feedback(feedback)       // ★ 피드백
                            .build();
                })
                .toList();

        // 4) 포스트 + 결과 구성
        PostDto post = PostDto.builder()
                .postId(detail.getPost().getPostId())
                .postTitle(detail.getPost().getPostTitle())
                .postDescription(detail.getPost().getPostDescription())
                .qaList(mapped)
                .build();

        return SimulationResultDto.builder()
                .simulationId(simulationId)
                .post(post)
                .build();
    }


    @Transactional(readOnly = true)
    public Optional<Simulation> findById(Long id) {
        return simulationRepository.findById(id);
    }
}
