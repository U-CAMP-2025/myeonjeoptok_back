package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "JOB")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id",  nullable = false)
    private Long jobId;

    @Column(name = "job_name",  nullable = false, length = 100)
    private String jobName;

}
