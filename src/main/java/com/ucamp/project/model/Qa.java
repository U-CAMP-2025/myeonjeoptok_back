package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;
//

@Entity
@Table(name = "QA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Qa {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "qa_seq_gen")
    @SequenceGenerator(
            name = "qa_seq_gen",
            sequenceName = "QA_SEQ",
            allocationSize = 1
    )
    @Column(name = "qa_id")
    private Long qaId;

    @Column(name = "qa_order", nullable = false)
    private Long qaOrder;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "qa_question", nullable = false, length = 500)
    private String qaQuestion;

    @Column(name = "qa_answer", length = 500)
    private String qaAnswer;

    @Transient
    private Long postId;

    @PrePersist
    private void onCreate() {
        if (this.post == null && this.postId != null) {
            this.post = Post.builder().postId(this.postId).build();
        }
    }

}
