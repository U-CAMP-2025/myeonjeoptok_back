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

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
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

    @Transient
    private Long userId;
    @Transient
    private Long interviewerId;
    @Transient
    private Long postId;

    @PrePersist
    private void onCreate() {
        if (this.user == null && this.userId != null) {
            this.user = User.builder().userId(userId).build();
        }
        if (this.interviewer == null && this.interviewerId != null) {
            this.interviewer = Interviewer.builder().interviewerId(this.interviewerId).build();
        }
        if (this.post == null && this.postId != null) {
            this.post = Post.builder().postId(this.postId).build();
        }
        if (this.simulationQACount == null) {
            this.simulationQACount = 0l;
        }
        if (this.simulationCreatedAt == null) {
            this.simulationCreatedAt = LocalDateTime.now();
        }
        if (this.simulationStatus == null) {
            this.simulationStatus = "INPROGRESS";
        }
    }


}
