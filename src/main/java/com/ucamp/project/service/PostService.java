package com.ucamp.project.service;


import com.ucamp.project.dto.SimualtionPostResponse;
import com.ucamp.project.model.Post;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.repository.PostJobRepository;
import com.ucamp.project.repository.PostRepository;
import com.ucamp.project.repository.SimulationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class PostService {

    private final PostRepository postRepository;

    private final PostJobRepository postJobRepository;
    
    public List<Post> findAll(){
        return postRepository.findAll();
    }

    public List<SimualtionPostResponse> simulGetPost(Long userId) {
//        List<Post> posts = postRepository.simulGetPost(userId);
        List<Post> posts = postRepository.findAll();
        List<SimualtionPostResponse> resp = new ArrayList<>();
        for(Post post : posts){
            System.out.println("test : "+post.getPostId());
            resp.add(SimualtionPostResponse.builder()
                            .postId(post.getPostId())
                            .title(post.getPostTitle())
                            .job(postJobRepository.findByPostId(post.getPostId()))
                    .build());
        }
        return resp;
    }
}
