package com.ucamp.project.service;


import com.ucamp.project.dto.PostCreateRequestDTO;
import com.ucamp.project.dto.PostResponseDTO;
import com.ucamp.project.dto.SimualtionPostResponse;
import com.ucamp.project.model.*;
import com.ucamp.project.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;

    private final PostJobRepository postJobRepository;

    private final JobRepository jobRepository;

    private final ReviewRepository reviewRepository;

    private final UserRepository userRepository;

    private final SimulationRepository simulationRepository;

    private final TranscriptionRepository transcriptionRepository;

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public List<SimualtionPostResponse> simulGetPost(Long userId) {
        List<Post> posts = postRepository.simulGetPost(userId);
        List<SimualtionPostResponse> resp = new ArrayList<>();
        for (Post post : posts) {
            System.out.println("test : " + post.getPostId());
            resp.add(SimualtionPostResponse.builder().postId(post.getPostId()).title(post.getPostTitle()).job(postJobRepository.findByPostId(post.getPostId())).build());
        }
        return resp;
    }

    public List<PostResponseDTO> findAllByUserId(Long userId) {
        List<Post> posts = postRepository.simulGetPost(userId);
        List<PostResponseDTO> resp = new ArrayList<>();
        for (Post post : posts) {
            System.out.println("test : " + post);


            resp.add(PostResponseDTO.builder().postId(post.getPostId()).nickname(post.getUser().getNickname()).otherWriter(post.getPostOtherWriter() == null ? null : post.getPostOtherWriter().getNickname()).job(postJobRepository.findByPostId(post.getPostId())).title(post.getPostTitle()).description(post.getPostDescription()).bookCount(post.getCount()).review(reviewRepository.findByPostPostId(post.getPostId()).toArray().length).createAt(post.getPostUpdatedAt() == null ? post.getPostCreatedAt() : post.getPostUpdatedAt()).build());
        }
        return resp;
    }

    @Transactional
    public Long createPost(PostCreateRequestDTO req, User user) {

        List<Post> postCount = postRepository.simulGetPost(user.getUserId());

        if (postCount.size() == 10) {
            throw new RuntimeException("질문셋은 최대 10개까지 생성됩니다.");
        }

        //포스트 생성
        Post post = Post.builder().postTitle(req.getTitle()).user(user).postDescription(req.getSummary()).postStatus(req.getStatus()).postOtherWriter(null).build();

//         PostID 값 받아오기
        Post postRecive = postRepository.save(post);

        for (Long jobId : req.getJobIds()) {

            PostJobId job = PostJobId.builder().post(postRecive).job(jobRepository.findById(jobId).get()).build();
            PostJob pJob = PostJob.builder().postJobId(job).build();
            postJobRepository.save(pJob);
        }

        long count = 1l;

        List<Qa> Qas = new ArrayList<>();
        for (PostCreateRequestDTO.QaSet qaSet : req.getQaSets()) {
            if (count++ >= 10) {
                break;
            }
            Qa qa = Qa.builder().post(postRecive).qaQuestion(qaSet.getQuestion()).qaAnswer(qaSet.getAnswer()).qaOrder(count++).build();
            Qas.add(qa);
        }
        postRecive.setQaList(Qas);

        return postRecive.getPostId();
    }

    @Transactional
    public PostResponseDTO findById(Long postId, User user) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("존재하지 않는 포스트 아이디 입니다."));

        List<PostCreateRequestDTO.QaSet> qaDtoList = post.getQaList().stream().map(qa -> PostCreateRequestDTO.QaSet.builder().qaId(qa.getQaId()).question(qa.getQaQuestion()).answer(qa.getQaAnswer()).build()).toList();

        boolean isMe = post.getUser().getUserId().equals(user.getUserId());
        boolean isPublic = post.getPostStatus().equals("N");

        if (isPublic && !isMe) {
            throw new RuntimeException("비공개 질문셋입니다");
        }

        return PostResponseDTO.builder().postId(post.getPostId()).job(postJobRepository.findByPostId(post.getPostId())).jobIds(postJobRepository.findByJobId(post.getPostId())).title(post.getPostTitle()).nickname(post.getUser().getNickname()).description(post.getPostDescription()).createAt(post.getPostUpdatedAt() == null ? post.getPostCreatedAt() : post.getPostUpdatedAt()).isPassed(post.getUser().getPassStatus() != null).isPublic(!isPublic).isMe(isMe).otherWriter(post.getPostOtherWriter() == null ? null : post.getPostOtherWriter().getNickname()).qa(qaDtoList).build();


    }

    @Transactional
    public Long updatePost(Long postId, PostCreateRequestDTO req, User user) {

        // 질문셋 조회
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("존재하지 않는 질문셋입니다."));

        // 유저 체크
        if (!user.getUserId().equals(post.getUser().getUserId())) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        // 질문셋 정보 반영
        post.setPostTitle(req.getTitle());
        post.setPostDescription(req.getSummary());
        post.setPostStatus(req.getStatus());
        post.setPostUpdatedAt(LocalDateTime.now());

        // JOB 갱신 -> 삭제후 재 기입
        postJobRepository.deleteAllByPost(post);
        for (Long jobId : req.getJobIds()) {
            PostJobId jobIdObj = PostJobId.builder().post(post).job(jobRepository.findById(jobId).get()).build();
            PostJob pJob = PostJob.builder().postJobId(jobIdObj).build();
            postJobRepository.save(pJob);
        }

        // QA 처리
        List<Qa> qaList = post.getQaList();

        // 1. 삭제할 QaId 목록을 수집합니다.
        List<Long> incomingQaIds = new ArrayList<>();
        for (PostCreateRequestDTO.QaSet qaSet : req.getQaSets()) {
            log.info("TTTTTEST : " + qaSet.getQaId());
            if (qaSet.getQaId() != null) {
                incomingQaIds.add(qaSet.getQaId());
            }
        }

        for (Iterator<Qa> iterator = qaList.iterator(); iterator.hasNext();) {
            Qa qa = iterator.next();

            // 요청에 없는 qaId를 처리
            if (qa.getQaId() != null && !incomingQaIds.contains(qa.getQaId())) {

                // 요청에 없는 qaId에 해당하는 transcription 삭제
                transcriptionRepository.deleteAllByQaQaId(qa.getQaId());

                // qaList에서 요청에 없는 qaId를 가진 항목 제거
                iterator.remove();
            }
        }


        long count = 1;
        for (PostCreateRequestDTO.QaSet qaSet : req.getQaSets()) {
            if (count > 10) break;

            Qa updateQa = null;
            if (qaSet.getQaId() != null) {
                for (Qa qa : qaList) {
                    if (qa.getQaId().equals(qaSet.getQaId())) {
                        updateQa = qa;
                        break;
                    }
                }
            }

            if (updateQa != null) {
                // 기존 QA 수정
                updateQa.setQaQuestion(qaSet.getQuestion());
                updateQa.setQaAnswer(qaSet.getAnswer());
                updateQa.setQaOrder(count++);
            } else {
                // 새 QA 추가
                Qa qa = Qa.builder().post(post).qaQuestion(qaSet.getQuestion()).qaAnswer(qaSet.getAnswer()).qaOrder(count++).build();
                qaList.add(qa);
            }
        }

        return post.getPostId();
    }

    @Transactional
    public Long deletePost(User user, Long postId) {
        Post post = postRepository.findByUserUserIdAndPostId(user.getUserId(), postId).orElseThrow(() -> new RuntimeException("존재하지 않는 질문셋입니다."));

        postJobRepository.deleteAllByPost(post);

        reviewRepository.deleteAllByPost(post);

        List<Simulation> sims = simulationRepository.findByPost(post);

        for (Simulation sim : sims) {
            transcriptionRepository.deleteBySimulation(sim);
            simulationRepository.deleteById(sim.getSimulationId());
        }

        postRepository.delete(post);

        return postId;
    }

    public Page<PostResponseDTO> getPosts(int page, int limit, String sort, List<Long> jobIds) {

        String[] parts = sort.split("_");
        String col = parts[0]; // latest / bookcount / review
        String dir = parts[1]; // asc / desc

        String sortColumn;
        switch (col) {
            case "latest":
                sortColumn = "post_created_at";
                break;
            case "bookcount":
                sortColumn = "post_import_count";
                break;
            case "review":
                sortColumn = "review"; // 예시
                break;
            default:
                sortColumn = "post_created_at"; // 기본값
        }

        Sort.Direction direction = dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sorted = Sort.by(direction, sortColumn);


        Pageable pageable = PageRequest.of(page - 1, limit, sorted);

        Page<Object[]> rawPage = null;

        if (jobIds.isEmpty()) {
            rawPage = postRepository.findPostsWithJoinsNotJobs(jobIds, pageable);
        } else {
            rawPage = postRepository.findPostsWithJoins(jobIds, pageable);
        }

        List<PostResponseDTO> dtoList = rawPage.stream().map(obj -> PostResponseDTO.builder().postId(((Number) obj[0]).longValue()).nickname((String) obj[1]).job(obj[2] != null && !((String) obj[2]).isEmpty() ? List.of(((String) obj[2]).split(",")) : List.of()).title((String) obj[3]).description((String) obj[4]).bookCount(((Number) obj[5]).longValue()).review(((Number) obj[6]).intValue()).createAt(((java.sql.Timestamp) obj[7]).toLocalDateTime()).isPublic(((Number) obj[8]).intValue() == 1).isPassed(((Number) obj[9]).intValue() == 1).build()).toList();

        for (PostResponseDTO resp : dtoList) {
            log.info("TEST : " + resp);
        }

        // Page 구현
        return new PageImpl<>(dtoList, pageable, rawPage.getTotalElements());
    }

    @Transactional
    public Long copyPost(Long postId, User user) {
        // 사용자가 작성한 질문셋 개수 체크
        List<Post> postCount = postRepository.simulGetPost(user.getUserId());
        if (postCount.size() == 10) {
            throw new RuntimeException("질문셋은 최대 10개까지 생성됩니다.");
        }

        // 원본 Post 조회
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("존재하지 않는 질문셋입니다."));

        post.setCount(post.getCount() + 1);

        // 원본 POST 복사
        Post copiedPost = Post.builder().postTitle(post.getPostTitle()).postDescription(post.getPostDescription()).user(user).postOtherWriter(post.getUser()).postStatus("N").build();

        // POST 생성
        Post postRecive = postRepository.save(copiedPost);

        // JOB 복사
        List<Long> jobs = postJobRepository.findByJobId(postId);

        // JOB 생성
        for (Long jobId : jobs) {

            PostJobId job = PostJobId.builder().post(postRecive).job(jobRepository.findById(jobId).get()).build();
            PostJob pJob = PostJob.builder().postJobId(job).build();
            postJobRepository.save(pJob);
        }

        // QA 복사
        List<Qa> copiedQas = new ArrayList<>();

        for (Qa qa : post.getQaList()) {
            Qa newQa = Qa.builder().post(postRecive).qaId(null).qaQuestion(qa.getQaQuestion()).qaAnswer(qa.getQaAnswer()).qaOrder(qa.getQaOrder()).build();
            copiedQas.add(newQa);
        }
        // QA 생성
        postRecive.setQaList(copiedQas);

        return postRecive.getPostId();
    }
}
