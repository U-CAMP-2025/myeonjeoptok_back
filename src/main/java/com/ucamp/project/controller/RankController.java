package com.ucamp.project.controller;

import com.ucamp.project.dto.RankResponse;
import com.ucamp.project.service.RankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rank")
@Slf4j
public class RankController {

    private final RankService rankService;

    @GetMapping("/bookmark")
    public ResponseEntity<List<RankResponse>> findAllBookmark(){
        List<RankResponse> rank = rankService.findAllBookmark();
        return ResponseEntity.ok(rank);
    }

    @GetMapping("/practice")
    public ResponseEntity<List<RankResponse>> findAllPractice(){
        List<RankResponse> rank = rankService.findAllPractice();
        return ResponseEntity.ok(rank);
    }


}
