package com.ucamp.project.service;


import com.ucamp.project.dto.*;
import com.ucamp.project.model.Interviewer;
import com.ucamp.project.model.Post;
import com.ucamp.project.model.Qa;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.repository.PostRepository;
import com.ucamp.project.repository.QaRepository;
import com.ucamp.project.repository.SimulationRepository;
import com.ucamp.project.repository.TranscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final QaRepository qaRepository;
    private final TranscriptionRepository  transcriptionRepository;

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
    public List<Qa> finalizeReplaceAndDelete(Long simulationId, FinalizeRequest req) {
        var sim = simulationRepository.findBySimulationId(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found: " + simulationId));
        var post = sim.getPost();
        if (post == null) throw new IllegalStateException("Simulation has no Post");

        // Post의 QA 맵
        Map<Long, Qa> qaMap = post.getQaList().stream()
                .collect(Collectors.toMap(Qa::getQaId, q -> q));

        for (var it : Optional.ofNullable(req.getQaList()).orElse(List.of())) {
            Long oldQaId = it.getQaId();
            String trans = Optional.ofNullable(it.getTransContent()).orElse("").trim();
            if (oldQaId == null || trans.isBlank()) continue;

            Qa old = qaMap.get(oldQaId);
            if (old == null) {
                // 방어: 같은 Post 소속 재확인
                old = qaRepository.findByIdAndPostId(oldQaId, post.getPostId()).orElse(null);
            }
            if (old == null) continue;

            Long order = old.getQaOrder();
            String question = old.getQaQuestion();

            // 1) 유니크 제약 회피: old를 임시 order로 이동
            long tempOrder = -order; // 또는 큰 숫자
            old.setQaOrder(tempOrder);
            qaRepository.save(old);
            qaRepository.flush(); // DB에 확정

            // 2) 새 QA 생성(원래 order/같은 질문/새 답변)
            Qa fresh = Qa.builder()
                    .post(post)
                    .qaOrder(order)
                    .qaQuestion(question)
                    .qaAnswer(trans.length() > 500 ? trans.substring(0, 500) : trans)
                    .build();
            qaRepository.save(fresh);
            qaRepository.flush(); // fresh.qaId 확보

            // 3) 모든 Transcription FK를 fresh로 치환(전역)
            transcriptionRepository.reassignAllQa(oldQaId, fresh.getQaId());
            // (옵션) 안전 확인
            if (transcriptionRepository.existsByQa_QaId(oldQaId)) {
                throw new IllegalStateException("FK 치환 실패: 여전히 oldQaId를 참조하는 행이 있습니다. oldQaId=" + oldQaId);
            }

            // 4) old QA 삭제 (이 시점이면 FK 없음 → ORA-02292 발생 X)
            qaRepository.deleteById(oldQaId);
            qaRepository.flush();
        }

        // 최종 목록 반환(qaOrder asc)
        return qaRepository.findAllByPostIdOrderByQaOrder(post.getPostId());
    }



}
