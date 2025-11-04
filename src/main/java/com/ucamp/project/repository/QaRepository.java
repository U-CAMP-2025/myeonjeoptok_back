package com.ucamp.project.repository;

import com.ucamp.project.model.Qa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QaRepository extends JpaRepository<Qa, Long> {
    @Query("""
      select q from Qa q
      join fetch q.post p
      where q.qaId = :qaId and p.postId = :postId
    """)
    Optional<Qa> findByIdAndPostId(@Param("qaId") Long qaId, @Param("postId") Long postId);

    @Query("""
    select q from Qa q
    where q.post.postId = :postId
    order by q.qaOrder asc
""")
    List<Qa> findAllByPostIdOrderByQaOrder(@Param("postId") Long postId);
}
