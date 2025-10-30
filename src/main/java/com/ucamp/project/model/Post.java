package com.ucamp.project.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;
//

@Entity
@Table(name = "POST")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "post_seq_gen")     // @SeqGen의 별명과 연결
    @SequenceGenerator(
            name = "post_seq_gen",      // generator과 연결할 별명 생성
            sequenceName = "POST_SEQ",  // DB에 생성한 시퀀스 이름과 연결
            allocationSize = 1          // 건너뜀 방지, 1개씩만 가져옴
    )
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "post_title", nullable = false, length = 30)
    private String postTitle;

    @Column(name = "post_description", length = 100)
    private String postDescription;

    @Column(name = "post_import_count",nullable = false)
    @ColumnDefault("0")
    private Long count;

    @Column(name = "post_created_at",  nullable = false)
    private LocalDate postCreatedAt;

    @Column(name = "post_update_at")
    private LocalDateTime postUpdatedAt;

    @Column(name = "post_status",length = 1)
    @ColumnDefault("'Y'")
    private String postStatus;

    @ManyToOne
    @JoinColumn(name = "post_other_writer")
    private User postOtherWriter;



}
