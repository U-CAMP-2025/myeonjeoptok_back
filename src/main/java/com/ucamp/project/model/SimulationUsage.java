package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SIMULATION_USAGE",
  uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "simulation_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationUsage  {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sim_usage_seq")
    @SequenceGenerator(name = "sim_usage_seq", sequenceName = "SIM_USAGE_SEQ", allocationSize = 1)
    @Column(name = "usage_id")
    private Long usageId;

    @ManyToOne @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "simulation_id", nullable = false)
    private Long simulationId; // 관계로 묶지 않고 숫자로만 보관 → 삭제 영향 없음

    @Column(name = "sim_date", nullable = false)
    private LocalDate simDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (simDate == null) simDate = createdAt.toLocalDate();
    }

}