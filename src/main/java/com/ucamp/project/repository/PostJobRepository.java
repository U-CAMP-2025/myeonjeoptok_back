package com.ucamp.project.repository;

import com.ucamp.project.model.Post;
import com.ucamp.project.model.PostJob;
import com.ucamp.project.model.PostJobId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostJobRepository extends JpaRepository<PostJob, PostJobId> {

    @Query("SELECT pj.postJobId.job.jobName FROM PostJob pj WHERE pj.postJobId.post.id = :postId")
    List<String> findByPostId(Long postId);



}
