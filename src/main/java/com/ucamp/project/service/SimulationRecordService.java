package com.ucamp.project.service;

import com.ucamp.project.dto.SimulationRecordItemDto;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.repository.PostJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationRecordService {

    private final SimulationService simulationService;
    private final PostJobRepository postJobRepository; // 추가

    @Transactional(readOnly = true)
    public List<SimulationRecordItemDto> listMyRecords(Long userId) {
        // 1️ 로그인 사용자의 시뮬레이션 리스트 조회
        List<Simulation> sims = simulationService.findByUserId(userId);

        // 2 각 시뮬레이션마다 Post 정보 + Job 태그 조회
        return sims.stream().map(sim -> {
            Long postId = sim.getPost().getPostId();

            //  PostJobRepository를 사용해서 직무 태그 목록 불러오기
            List<String> jobs = postJobRepository.findByPostId(postId);

            return SimulationRecordItemDto.builder()
                    .simulationId(sim.getSimulationId())
                    .simulationStatus(sim.getSimulationStatus())
                    .post(
                            SimulationRecordItemDto.PostBrief.builder()
                                    .postId(postId)
                                    .title(sim.getPost().getPostTitle()) // post_title 컬럼 기준
                                    .job(jobs)
                                    .build()
                    )
                    .build();
        }).toList();
    }
}
