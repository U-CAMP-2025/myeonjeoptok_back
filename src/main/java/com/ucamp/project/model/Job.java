package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;
//

@Entity
@Table(name = "JOB")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "job_seq_gen")     // @SeqGen의 별명과 연결
    @SequenceGenerator(
            name = "job_seq_gen",      // generator과 연결할 별명 생성
            sequenceName = "JOB_SEQ", // DB에 생성한 시퀀스 이름과 연결
            allocationSize = 1          // 건너뜀 방지, 1개씩만 가져옴
    )
    @Column(name = "job_id",  nullable = false)
    private Long jobId;

    @Column(name = "job_name",  nullable = false, length = 100)
    private String jobName;

}
