package com.ucamp.project.repository;

import com.ucamp.project.dto.ReviewResponse;
import com.ucamp.project.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPost_PostId(Long postId);
}
