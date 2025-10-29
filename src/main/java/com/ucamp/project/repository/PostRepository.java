package com.ucamp.project.repository;

import com.ucamp.project.dto.SimualtionPostResponse;
import com.ucamp.project.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE user.userId = :userId")
    List<Post> simulGetPost(@Param("userId") Long userId);

}
