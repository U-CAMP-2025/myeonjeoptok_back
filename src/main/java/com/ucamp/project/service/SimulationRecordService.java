package com.ucamp.project.service;

import com.ucamp.project.dto.SimulationRecordItemDto;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.repository.PostJobRepository;
import com.ucamp.project.repository.SimulationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimulationRecordService {

    private final SimulationService simulationService;
    private final PostJobRepository postJobRepository; // 추가
    private final SimulationRepository simulationRepository;

    @Transactional(readOnly = true)
    public List<SimulationRecordItemDto> listMyRecords(Long userId) {
        // 1) 로그인 사용자의 전체 시뮬레이션 조회
        List<Simulation> sims = simulationService.findByUserId(userId);

        // ✅ 완료된 시뮬레이션만 필터링 (SUCCESS 상태만)
        List<Simulation> completedSims = sims.stream()
                .filter(sim -> "SUCCESS".equalsIgnoreCase(sim.getSimulationStatus()))
                .collect(Collectors.toList());

        // 2) Post 기준 그룹핑
        Map<Long, List<Simulation>> groupedByPost = completedSims.stream()
                .collect(Collectors.groupingBy(sim -> sim.getPost().getPostId()));

        // 3) 그룹별로 DTO 생성
        return groupedByPost.entrySet().stream()
                .map(entry -> {
                    Long postId = entry.getKey();
                    List<Simulation> group = entry.getValue();

                    List<String> jobs = postJobRepository.findByPostId(postId);

                    long repetitionCount = group.size(); // ✅ 완료된 것만 카운트

                    Optional<Simulation> latestCompletedOpt = group.stream()
                            .filter(g -> g.getSimulationCompletedAt() != null)
                            .max(Comparator.comparing(Simulation::getSimulationCompletedAt));

                    LocalDateTime latestCompletedAt = latestCompletedOpt
                            .map(Simulation::getSimulationCompletedAt)
                            .orElse(null);

                    Simulation representative = latestCompletedOpt.orElseGet(() ->
                            group.stream()
                                    .max(Comparator.comparing(Simulation::getSimulationCreatedAt))
                                    .orElse(group.get(0))
                    );

                    return SimulationRecordItemDto.builder()
                            .simulationId(representative.getSimulationId())
                            .simulationStatus(representative.getSimulationStatus())
                            .completedAt(latestCompletedAt)
                            .count(repetitionCount)
                            .post(SimulationRecordItemDto.PostBrief.builder()
                                    .postId(postId)
                                    .title(representative.getPost().getPostTitle())
                                    .job(jobs)
                                    .build())
                            .build();
                })
                .sorted((a, b) -> {
                    LocalDateTime aKey = a.getCompletedAt();
                    LocalDateTime bKey = b.getCompletedAt();
                    if (aKey == null && bKey == null) return 0;
                    if (aKey == null) return 1;
                    if (bKey == null) return -1;
                    return bKey.compareTo(aKey);
                })
                .collect(Collectors.toList());
    }
}
