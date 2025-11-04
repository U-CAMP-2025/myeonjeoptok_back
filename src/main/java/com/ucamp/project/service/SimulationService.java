package com.ucamp.project.service;


import com.ucamp.project.dto.*;
import com.ucamp.project.model.Interviewer;
import com.ucamp.project.model.Post;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.repository.PostRepository;
import com.ucamp.project.repository.SimulationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final PostRepository postRepository;
    public List<Simulation> findAll(){
        return simulationRepository.findAll();
    }

    public Simulation save(Simulation simulation) {
        return simulationRepository.save(simulation);
    }
    public Simulation findById(Long id) {return  simulationRepository.findById(id).get();}
    public void ensureOwner(Long simulationId, Long userId) {
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시뮬레이션입니다."));

        if (!simulation.getUser().getUserId().equals(userId)) {
            throw new SecurityException("본인의 시뮬레이션이 아닙니다.");
        }
    }

    public SimulationDetailResponse findDetail(Long simulationId) {
        Simulation sim = simulationRepository.findBySimulationId(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found: " + simulationId));

        // interviewer 매핑
        Interviewer interviewer = sim.getInterviewer();
        InterviewerDto interviewerDto = InterviewerDto.builder()
                .interviewerId(interviewer.getInterviewerId())
                .interviewerImageUrl(interviewer.getInterviewerImageUrl())
                .build();

        // post + qa 리스트 매핑
        Post post = sim.getPost();
        List<QaDto> qaDtos = post.getQaList().stream()
                .map(q -> QaDto.builder()
                        .qaId(q.getQaId())
                        .qaOrder(q.getQaOrder())
                        .qaQuestion(q.getQaQuestion())
                        .qaAnswer(q.getQaAnswer())
                        .build())
                .toList();

        PostDto postDto = PostDto.builder()
                .postId(post.getPostId())
                .postTitle(post.getPostTitle())
                .postDescription(post.getPostDescription())
                .qaList(qaDtos)
                .build();

        return SimulationDetailResponse.builder()
                .interviewer(interviewerDto)
                .post(postDto)
                .simulationRandom(sim.getSimulationRandom())
                .build();
    }


    public List<Simulation> findByUserId(Long userId) {
        return simulationRepository.findByUser_UserIdOrderBySimulationIdDesc(userId);
    }

    @Transactional
    public void finalizeToPost(SimulationResultDto result) {
        Long postId = result.getPost().getPostId();

        // Post 및 QA 엔티티 로드
        Post post = postRepository.findByIdFetchQa(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Post가 존재하지 않습니다."));

        // qaList 순회하며 transContent -> qaAnswer로 덮어쓰기
        for (QaDto qaDto : result.getPost().getQaList()) {
            post.getQaList().stream()
                    .filter(q -> q.getQaId().equals(qaDto.getQaId()))
                    .findFirst()
                    .ifPresent(q -> {
                        if (qaDto.getTransContent() != null && !qaDto.getTransContent().isBlank()) {
                            q.setQaAnswer(qaDto.getTransContent().trim());
                        }
                    });
        }

        // 상태 갱신 등 필요 시 추가
        // post.setStatus("FINALIZED");

        postRepository.save(post);
    }
}
