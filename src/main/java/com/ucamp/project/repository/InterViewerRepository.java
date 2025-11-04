package com.ucamp.project.repository;

import com.ucamp.project.model.Interviewer;
import com.ucamp.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterViewerRepository extends JpaRepository<Interviewer, Long> {
}
