package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "SIMULATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Simulation {
    //
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "simulation_seq_gen")
    @SequenceGenerator(
            name = "simulation_seq_gen",
            sequenceName = "SIMULATION_SEQ",
            allocationSize = 1
    )
    @Column(name = "simulation_id")
    private Long simulationId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "interviewer_id", nullable = false)
    private Interviewer interviewer;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "simulation_random",length = 1)
    @ColumnDefault("'N'")
    private String simulationRandom;

    @Column(name = "simulation_qa_count")
    private Long simulationQACount;

    @Column(name = "simulation_created_at")
    private LocalDateTime simulationCreatedAt;

    @Column(name = "simulation_completed_at")
    private LocalDateTime simulationCompletedAt;

    @Column(name = "simulation_status",length = 20)
    private String simulationStatus;
//* SIMULATION_CREATED_AT DATE
//* SIMULATION_COMPLETED_AT DATE
//    SIMULATION_STATUS VARCHAR2 (20 BYTE)


}
