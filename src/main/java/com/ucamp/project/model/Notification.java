package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
//

@Entity
@Table(name = "NOTIFICATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "notification_seq_gen")     // @SeqGen의 별명과 연결
    @SequenceGenerator(
            name = "notification_seq_gen",      // generator과 연결할 별명 생성
            sequenceName = "NOTIFICATION_SEQ", // DB에 생성한 시퀀스 이름과 연결
            allocationSize = 1          // 건너뜀 방지, 1개씩만 가져옴
    )
    @Column(name = "noti_id")
    private Long notiId;

    @Column(name = "noti_content", length = 100)
    private String notiContent;

    @Column(name = "noti_type",length = 10)
    private String notiType;

    @Column(name = "noti_read",length = 1)
    @ColumnDefault("'Y'")
    private String notiRead;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "noti_created_at")
    private LocalDateTime notiCreatedAt;
}
