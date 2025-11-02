package com.ucamp.project.controller;

import com.ucamp.project.dto.ApiResponse;
import com.ucamp.project.dto.PostCreateRequestDTO;
import com.ucamp.project.dto.PostScrollRequest;
import com.ucamp.project.model.User;
import com.ucamp.project.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Slf4j
public class PostController {

    private final PostService postService;

    @GetMapping("/my")
    public ApiResponse<?> myPosts(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User)auth.getPrincipal();
        return ApiResponse.builder().code(200).message("success").data(postService.findAllByUserId(user.getUserId())).build();
    }

    @PostMapping("/search")
    public ApiResponse<?> allPosts(@RequestBody PostScrollRequest req){

        return ApiResponse.builder().code(200).message("success").data(postService.getPosts(req.getPage(), req.getLimit(), req.getSort(), req.getJobs())).build();
    }

    @PostMapping
    public ApiResponse<?> createPost(@RequestBody PostCreateRequestDTO req){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User)auth.getPrincipal();
        log.info("TEST" + user.getUserId());
        log.info("TEST" + req);
        Long postId = postService.createPost(req, user);
        return ApiResponse.builder().code(201).message("success").data(postId).build();
    }

    @GetMapping("/{postId}")
    public ApiResponse<?> selectOne(@PathVariable Long postId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User)auth.getPrincipal();

        return ApiResponse.builder().code(200).message("success").data(postService.findById(postId, user)).build();
    }

    @PutMapping("/{postId}")
    public ApiResponse<?> updatePost(@PathVariable Long postId, @RequestBody PostCreateRequestDTO req){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User)auth.getPrincipal();
        log.info("TEST" + user.getUserId());
        log.info("TEST" + req);
        postService.updatePost(postId, req, user);
        return ApiResponse.builder().code(201).message("success").data(postId).build();
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<?> deleteOne(@PathVariable Long postId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User)auth.getPrincipal();

        return ApiResponse.builder().code(200).message("success").data(postService.deletePost(user,postId)).build();
    }

    @PostMapping("/{postId}")
    public ApiResponse<?> copyPost(@PathVariable Long postId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User)auth.getPrincipal();

        Long copyPostId = postService.copyPost(postId, user);

        return ApiResponse.builder().code(201).message("success").data(copyPostId).build();
    }
}
