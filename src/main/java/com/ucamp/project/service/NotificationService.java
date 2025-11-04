package com.ucamp.project.service;


import com.ucamp.project.dto.NotificationResponse;
import com.ucamp.project.model.Notification;
import com.ucamp.project.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class NotificationService {


    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> findAll(Long userId) {
        List<NotificationResponse> resp = new ArrayList<>();
        for(Notification noti: notificationRepository.findAllByUserId(userId)){
            resp.add(NotificationResponse.builder()
                    .notiId(noti.getNotiId())
                    .type(noti.getNotiType())
                    .content(noti.getNotiContent())
                    .createdAt(noti.getNotiCreatedAt())
                    .read(noti.getNotiRead().equals("Y"))
                    .build());
        }
        return resp;
    }

    @Transactional
    public void readOne(Long notiId, Long userId) {
        Optional<Notification> noti =  notificationRepository.findById(notiId);
        if(!Objects.equals(noti.get().getUser().getUserId(), userId)){
            throw new RuntimeException("접근 권한이 없습니다.");
        }
        noti.get().setNotiRead("Y");
    }

    @Transactional
    public void readAll(Long userId) {
        for(Notification noti : notificationRepository.findAllByUserId(101l)){
            noti.setNotiRead("Y");
        }
    }

    @Transactional
    public void delOne(Long notiId, long userId) {
        Optional<Notification> noti =  notificationRepository.findById(notiId);
        if(!Objects.equals(noti.get().getUser().getUserId(), userId)){
            throw new RuntimeException("접근 권한이 없습니다.");
        }
        notificationRepository.delete(noti.get());
    }

    @Transactional
    public void delAll(long userId) {
        List<Notification> notis =  notificationRepository.findAllByUserId(userId);
        if(!notis.isEmpty()){
            if(notis.getFirst().getUser().getUserId() != userId){
                throw new RuntimeException("접근 권한이 없습니다.");
            }
        }
        for(Notification noti: notis){
            notificationRepository.delete(noti);
        }
    }
}
