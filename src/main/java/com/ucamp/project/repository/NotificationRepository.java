package com.ucamp.project.repository;

import com.ucamp.project.dto.NotificationResponse;
import com.ucamp.project.model.Notification;
import com.ucamp.project.model.Simulation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {


    @Query("SELECT noti FROM Notification noti WHERE noti.user.userId = :userId ORDER BY noti.notiRead, noti.notiCreatedAt")
    List<Notification> findAllByUserId(@Param("userId") Long userId);

    Page<Notification> findByUserUserId(Long userId, Pageable pageable);

    boolean existsByUser_UserIdAndNotiTypeAndNotiContentAndNotiCreatedAtBetween(
            Long userId, String notiType, String notiContent,
            LocalDateTime startOfDay, LocalDateTime endOfDay
    );
}
