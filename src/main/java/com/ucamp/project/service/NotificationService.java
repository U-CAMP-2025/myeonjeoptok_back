package com.ucamp.project.service;


import com.ucamp.project.dto.NotificationResponse;
import com.ucamp.project.model.Notification;
import com.ucamp.project.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Not;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationService {


    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> findAll(Long userId) {

        Pageable pageable = PageRequest.of(0, 99, Sort.by(Sort.Order.asc("notiCreatedAt")));
        Page<Notification> notis = notificationRepository.findByUserUserId(userId, pageable);

        List<Notification> notificationList = new ArrayList<>(notis.getContent());

        if (!notis.getContent().isEmpty()) {

            notificationList.remove(notificationList.size()-1);

            if(notificationList.isEmpty()){
                return null;
            }
        }

        List<NotificationResponse> resp = new ArrayList<>();

        for (Notification noti : notificationList) {
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

    public NotificationResponse findOne(Long userId) {

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("notiCreatedAt")));
        Page<Notification> notis = notificationRepository.findByUserUserId(userId, pageable);
        List<Notification> notificationList = new ArrayList<>(notis.getContent());
        Notification noti = notificationList.isEmpty() ? null : notificationList.getFirst();

        if(noti == null){
            return null;
        }

        return NotificationResponse.builder()
                .notiId(noti.getNotiId())
                .type(noti.getNotiType())
                .content(noti.getNotiContent())
                .createdAt(noti.getNotiCreatedAt())
                .read(noti.getNotiRead().equals("Y"))
                .build();
    }

    @Transactional
    public void readOne(Long notiId, Long userId) {
        Optional<Notification> noti = notificationRepository.findById(notiId);
        if (!Objects.equals(noti.get().getUser().getUserId(), userId)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }
        noti.get().setNotiRead("Y");
    }

    @Transactional
    public void readAll(Long userId) {
        for (Notification noti : notificationRepository.findAllByUserId(userId)) {
            noti.setNotiRead("Y");
        }
    }

    @Transactional
    public void delOne(Long notiId, long userId) {
        Optional<Notification> noti = notificationRepository.findById(notiId);
        if (!Objects.equals(noti.get().getUser().getUserId(), userId)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }
        notificationRepository.delete(noti.get());
    }

    @Transactional
    public void delAll(long userId) {
        List<Notification> notis = notificationRepository.findAllByUserId(userId);
        if (!notis.isEmpty()) {
            if (notis.getFirst().getUser().getUserId() != userId) {
                throw new RuntimeException("접근 권한이 없습니다.");
            }
        }
        for (Notification noti : notis) {
            notificationRepository.delete(noti);
        }
    }
}
