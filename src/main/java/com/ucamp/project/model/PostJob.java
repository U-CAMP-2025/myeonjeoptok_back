package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "POST_JOB")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostJob {
    @EmbeddedId
    private PostJobId postJobId;
}
