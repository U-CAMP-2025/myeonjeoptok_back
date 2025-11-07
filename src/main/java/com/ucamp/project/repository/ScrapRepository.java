package com.ucamp.project.repository;

import com.ucamp.project.model.Scrap;
import com.ucamp.project.model.ScrapId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, ScrapId> {

}
