package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;
//

@Entity
@Table(name = "INTERVIEWER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interviewer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "interviewer_seq_gen")     // @SeqGen의 별명과 연결
    @SequenceGenerator(
            name = "interviewer_seq_gen",      // generator과 연결할 별명 생성
            sequenceName = "INTERVIEWER_SEQ", // DB에 생성한 시퀀스 이름과 연결
            allocationSize = 1          // 건너뜀 방지, 1개씩만 가져옴
    )
    @Column(name = "interviewer_id")
    private Long interviewerId;

    @Column(name = "interviewer_character_desc", length = 100)
    private String interviewerCharacterDesc;

    @Column(name = "interviewer_image_url",length = 200)
    private String interviewerImageUrl;
}
