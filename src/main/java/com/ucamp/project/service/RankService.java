package com.ucamp.project.service;

import com.ucamp.project.dto.RankResponse;
import com.ucamp.project.model.User;
import com.ucamp.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankService {
    private final UserRepository userRepository;

    public List<RankResponse> findAllBookmark(){

        List<Object[]> users = userRepository.findAllBookmark();
        log.info("users: {}", users);
        System.out.println("users: " + users);
//        int count =0;
//        for(Object[] temp: users){
//            log.info("user: "+count++);
//            for(Object t: temp){
//                log.info(t.toString());
//            }
//
//        }

        List<RankResponse> userList = users.stream()
                .map(row -> RankResponse.builder()
                        .userId(((Number) row[0]).longValue())
                        .nickname((String) row[1])
                        .passStatus((Character) row[2])
                        .profileImageUrl((String) row[3])
                        .jobName((String) row[4])
                        .cnt(((Number) row[5]).longValue())
                        .build())
                .toList();

        return userList;

    };
    public List<RankResponse> findAllPractice(String period){
        List<Object[]> users = userRepository.findAllPractice(period);
        log.info("users: {}", users);

        List<RankResponse> userList = users.stream()
                .map(row -> RankResponse.builder()
                        .userId(((Number) row[0]).longValue())
                        .nickname((String) row[1])
                        .passStatus((Character) row[2])
                        .profileImageUrl((String) row[3])
                        .jobName((String) row[4])
                        .cnt(((Number) row[5]).longValue())
                        .build())
                .toList();

        return userList;
    }

}
