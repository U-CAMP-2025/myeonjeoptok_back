package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "INTERVIEWER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interviewer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interviewer_id")
    private Long interviewerId;

    @Column(name = "interviewer_character_desc", length = 100)
    private String interviewerCharacterDesc;

    @Column(name = "interviewer_image_url",length = 200)
    private String interviewerImageUrl;
}
