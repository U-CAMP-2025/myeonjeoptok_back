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
        // 시뮬 상세
        SimulationDetailResponse detail = simulationService.findDetail(simulationId);

        Map<Long, String> trMap = transcriptionService.findAllBySimulation(simulationId).stream()
                // 키가 될 qaId null
                .filter(t -> t.getQa() != null && t.getQa().getQaId() != null)

                .sorted(Comparator.comparing(Transcription::getCompletedAt,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.toMap(
                        t -> t.getQa().getQaId(),
                        t -> Optional.ofNullable(t.getTrAnswerText()).orElse(""),
                        (prev, curr) -> curr
                ));

        List<QaDto> mapped = detail.getPost().getQaList().stream()
                .map(qa -> QaDto.builder()
                        .qaId(qa.getQaId())
                        .qaOrder(qa.getQaOrder())
                        .qaQuestion(qa.getQaQuestion())
                        .qaAnswer(qa.getQaAnswer())
                        .transContent(trMap.getOrDefault(qa.getQaId(), "")) // 핵심
                        .build())
                .toList();

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
