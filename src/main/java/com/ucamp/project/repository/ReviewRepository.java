package com.ucamp.project.repository;

import com.ucamp.project.model.Post;
import com.ucamp.project.model.Qa;
import com.ucamp.project.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.ucamp.project.dto.ReviewResponse;
import com.ucamp.project.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByPostPostId(Long postId);

    @Modifying
    @Query("DELETE FROM Review re WHERE re.post = :post")
    void deleteAllByPost(@Param("post") Post post);

    @Query("SELECT r FROM Review r WHERE r.post.postId = :postId ORDER BY r.reviewCreatedAt ASC")
    List<Review> findByPost_PostId(@Param("postId") Long postId);

}
