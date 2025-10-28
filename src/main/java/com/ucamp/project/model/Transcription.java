package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TRANSCRIPTION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transcription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tr_id")
    private Long trId;

    @ManyToOne
    @JoinColumn(name = "simulation_id")
    private Simulation simulation;

    @Column(name = "tr_answer_text",length = 500)
    private String trAnswerText;

    @ManyToOne
    @JoinColumn(name = "qa_id")
    private Qa qa;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
