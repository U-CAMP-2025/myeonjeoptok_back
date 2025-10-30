package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

//
@Entity
@Table(name = "TRANSCRIPTION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transcription {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "transcription_seq_gen")
    @SequenceGenerator(
            name = "transcription_seq_gen",
            sequenceName = "TRANSCRIPTION_SEQ",
            allocationSize = 1
    )
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

    @Transient
    private Long simulationId;

    @Transient
    private Long qaId;

    @PrePersist
    private void onCreate() {
        if (this.completedAt == null) {
            this.completedAt = java.time.LocalDateTime.now();
        }
        if (this.simulation == null && this.simulationId != null) {
            this.simulation = Simulation.builder().simulationId(this.simulationId).build();
        }
        if (this.qa == null && this.qaId != null) {
            this.qa = Qa.builder().qaId(this.qaId).build();
        }
    }
}
