package com.ucamp.project.model;

import lombok.*;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class ScrapId implements Serializable {
    private Long post;
    private Long user;
}