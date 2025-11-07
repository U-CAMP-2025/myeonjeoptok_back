package com.ucamp.project.repository;

import com.ucamp.project.dto.NotificationResponse;
import com.ucamp.project.model.Notification;
import com.ucamp.project.model.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {


    @Query("SELECT noti FROM Notification noti WHERE noti.user.userId = :userId ORDER BY noti.notiRead, noti.notiCreatedAt")
    List<Notification> findAllByUserId(@Param("userId") Long userId);

}
