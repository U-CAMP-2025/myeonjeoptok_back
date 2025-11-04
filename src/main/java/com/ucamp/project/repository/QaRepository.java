package com.ucamp.project.repository;

import com.ucamp.project.model.Qa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QaRepository extends JpaRepository<Qa, Long> {

}
