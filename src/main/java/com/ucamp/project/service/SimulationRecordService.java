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

        // 2) Post 기준 그룹핑
        Map<Long, List<Simulation>> groupedByPost = sims.stream()
                .collect(Collectors.groupingBy(sim -> sim.getPost().getPostId()));

        // 3) 그룹별로 집계하여 DTO 생성
        List<SimulationRecordItemDto> items = groupedByPost.entrySet().stream()
                .map(entry -> {
                    Long postId = entry.getKey();
                    List<Simulation> group = entry.getValue();

                    // 직무 태그
                    List<String> jobs = postJobRepository.findByPostId(postId);

                    // 동일 Post 반복 횟수(시도 수)
                    long repetitionCount = group.size();

                    // 최신 완료(완료 일시가 있는 것 중 최댓값)
                    Optional<Simulation> latestCompletedOpt = group.stream()
                            .filter(g -> g.getSimulationCompletedAt() != null)
                            .max(Comparator.comparing(Simulation::getSimulationCompletedAt));

                    LocalDateTime latestCompletedAt = latestCompletedOpt
                            .map(Simulation::getSimulationCompletedAt)
                            .orElse(null);

                    // 대표 시뮬레이션:
                    //  - 최신 완료가 있으면 그 시뮬레이션
                    //  - 없으면 simulationCreatedAt 기준 최신
                    Simulation representative = latestCompletedOpt.orElseGet(() ->
                            group.stream()
                                    .max(Comparator.comparing(Simulation::getSimulationCreatedAt))
                                    .orElse(group.get(0))
                    );

                    return SimulationRecordItemDto.builder()
                            .simulationId(representative.getSimulationId())             // 결과 링크용 대표 simId
                            .simulationStatus(representative.getSimulationStatus())     // 대표 상태(INPROGRESS/COMPLETED)
                            .completedAt(latestCompletedAt)                              // 최신 완료 일시(없으면 null)
                            .count(repetitionCount)                                      // 동일 Post 총 시도 수
                            .post(SimulationRecordItemDto.PostBrief.builder()
                                    .postId(postId)
                                    .title(representative.getPost().getPostTitle())
                                    .job(jobs)
                                    .build())
                            .build();
                })
                // 정렬: 최신 완료일 내림차순, 없으면 생성일 최신 우선
                .sorted((a, b) -> {
                    LocalDateTime aKey = a.getCompletedAt();
                    LocalDateTime bKey = b.getCompletedAt();
                    if (aKey == null && bKey == null) return 0;
                    if (aKey == null) return 1;
                    if (bKey == null) return -1;
                    return bKey.compareTo(aKey);
                })
                .collect(Collectors.toList());

        return items;
    }

}
