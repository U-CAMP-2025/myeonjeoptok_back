package com.ucamp.project.repository;

import com.ucamp.project.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

}
